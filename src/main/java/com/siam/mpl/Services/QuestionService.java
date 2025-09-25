package com.siam.mpl.Services;

import com.siam.mpl.DTOs.NewQuestionDto;
import com.siam.mpl.DTOs.QuestionDto;
import com.siam.mpl.DTOs.QuestionResponseDto;
import com.siam.mpl.DTOs.QuestionUpdateDto;
import com.siam.mpl.Entities.Question;
import com.siam.mpl.Entities.TeamQuestion;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Repositories.QuestionDao;
import com.siam.mpl.Repositories.TeamDao;
import com.siam.mpl.Repositories.TeamQuestionDao;
import jakarta.persistence.PessimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class QuestionService {
    //fields
    TeamDao teamDao;
    QuestionDao questionDao;
    TeamQuestionDao teamQuestionDao;

    //dependency injection
    public QuestionService(TeamDao teamDao, QuestionDao questionDao, TeamQuestionDao teamQuestionDao) {
        this.teamDao = teamDao;
        this.questionDao = questionDao;
        this.teamQuestionDao = teamQuestionDao;
    }

    @Retryable(
            value = {
                    DeadlockLoserDataAccessException.class,
                    CannotAcquireLockException.class,
                    PessimisticLockException.class
            },
            maxAttempts = 4,  // Try 4 times total (1 initial + 3 retries)
            backoff = @Backoff(
                    delay = 50,      // Start with 50ms delay
                    multiplier = 2,  // Double each time: 50ms, 100ms, 200ms
                    maxDelay = 1000, // Cap at 1 second
                    random = true    // Add randomness to prevent thundering herd
    ))
    @Transactional
    public Question tryToGetQuestion(QuestionDto questionDto) {

        log.debug("Processing question request for team: {}", questionDto.getTeamName());

        try {
            return getQuestion(questionDto);

        } catch (DeadlockLoserDataAccessException ex) {
            log.warn("Deadlock detected for team: {}, attempt will be retried",
                    questionDto.getTeamName());
            throw ex; // Re-throw to trigger retry
        }
    }

    //method to get the main question for each team from db
    @Transactional
    public Question getQuestion(QuestionDto questionDto) {
        Optional<Question> optionalQuestion = questionDao.findByIdWithLock(questionDto.getQuestionId());

        if(optionalQuestion.isEmpty()) {
            log.error("No question found with the given id for team {}", questionDto.getTeamName());
            throw new RuntimeException("No question with the given id found!");
        }

        Question question = optionalQuestion.get();
        String teamName = questionDto.getTeamName();

        //fetch team with pessimistic lock
        Optional<Teams> optionalTeam = teamDao.findByTeamName(teamName);

        //if team exists then return its assigned question
        if(optionalTeam.isPresent()) {
            log.info("Team {} has already got a question!", optionalTeam.get().getTeamName());
            Optional<TeamQuestion> optionalTeamQuestion = teamQuestionDao.findByTeamId(optionalTeam.get().getId());
            return optionalTeamQuestion.get().getQuestion();
        }

        saveTeamAndAssignQuestion(teamName, question);

        return question;
    }

    //method to save team in db and assign them a question
    public void saveTeamAndAssignQuestion(String teamName, Question question) {
        Optional<TeamQuestion> optionalTeamQuestion = teamQuestionDao.findByQuestionId(question.getId());

        if(optionalTeamQuestion.isPresent()) {
            log.error("The question that team {} is trying to get is already allotted to some team", teamName);
            throw new RuntimeException("This question is already allotted to some team");
        }

        //create new team and save in db
        Teams newTeam = new Teams();
        newTeam.setTeamName(teamName);
        newTeam.setPoints(1000);
        newTeam.setEndTime(LocalDateTime.now().plus(Duration.ofMinutes(30)));

        //check for concurrent transactions passing the initial check
        //unique constraint acting as last line of defence
        try {
            teamDao.save(newTeam);
            log.info("Team {} successfully registered!", teamName);
        } catch(DataIntegrityViolationException ex) {
            throw new RuntimeException("Team with this name already exists!");
        }

        //assign the new team a question
        TeamQuestion newTeamQuestion = new TeamQuestion();
        newTeamQuestion.setTeam(newTeam);
        newTeamQuestion.setQuestion(question);

        //check for concurrent transactions passing the initial check
        //unique constraint acting as last line of defence
        try {
            teamQuestionDao.save(newTeamQuestion);
            log.info("Team {} was allotted question {} successfully!", teamName, question.getId());
        } catch(DataIntegrityViolationException exception) {
            throw new RuntimeException("This question is already allotted to a team!");
        }
    }

    @Recover
    public Question recoverFromDeadlock(Exception ex, QuestionDto questionDto) {
        log.error("Failed to get question for team: {} after all retry attempts. Error: {}",
                questionDto.getTeamName(), ex.getMessage());

        //Return user-friendly error
        throw new RuntimeException(
                "Service is experiencing high traffic. Please try again in a few moments.");
    }

    //method to add a new question
    @Transactional
    public Question addQuestion(NewQuestionDto newQuestionDto) {
        Optional<Question> optionalQuestion = questionDao.findById(newQuestionDto.getQuestionId());
        if(optionalQuestion.isPresent()) {
            throw new RuntimeException("This id already exists. Please enter another id!");
        }

        Question newQuestion = new Question();
        newQuestion.setId(newQuestionDto.getQuestionId());
        newQuestion.setQuestion(newQuestionDto.getQuestion());
        return questionDao.save(newQuestion);
    }

    //method to delete a question
    @Transactional
    public Question removeQuestion(String questionId) {
        Optional<Question> optionalQuestion = questionDao.findById(questionId);

        if(optionalQuestion.isEmpty()) {
            throw new RuntimeException("No question with the give id exists!");
        }

        Question questionToBeRemoved = optionalQuestion.get();

        Optional<TeamQuestion> optionalTeamQuestion = teamQuestionDao.findByQuestionId(questionToBeRemoved.getId());

        if(optionalTeamQuestion.isPresent()) {
            throw new RuntimeException("The question is already allotted to a team!");
        }

        questionDao.delete(questionToBeRemoved);

        return questionToBeRemoved;
    }

    //method to update question description
    @Transactional
    public Question updateQuestion(String questionId, QuestionUpdateDto questionUpdateDto) {
        Optional<Question> optionalQuestion = questionDao.findById(questionId);

        if(optionalQuestion.isEmpty()) {
            throw new RuntimeException("No question with the given id found!");
        }

        Question questionForUpdate = optionalQuestion.get();

        if(questionUpdateDto.getQuestion() != null) {
            questionForUpdate.setQuestion(questionUpdateDto.getQuestion());
        }

        return questionDao.save(questionForUpdate);
    }

    //method to get all questions
    @Transactional
    public List<Question> getAllQuestions() {
        List<Question> questions = questionDao.findAll();
        if(questions.isEmpty()) {
            throw new RuntimeException("No questions found!");
        } else {
            return questions;
        }
    }

    //method to handle question submit
    @Transactional
    public void handleQuestionSubmission(QuestionResponseDto questionResponseDto) {
        Optional<Teams> optionalTeam = teamDao.findByTeamName(questionResponseDto.getTeamName());

        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given name found!");
        }

        optionalTeam.get().setPoints(optionalTeam.get().getPoints() + 100);
    }
}