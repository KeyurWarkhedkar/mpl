package com.siam.mpl.Services;

import com.siam.mpl.DTOs.NewQuestionDto;
import com.siam.mpl.DTOs.QuestionDto;
import com.siam.mpl.DTOs.QuestionUpdateDto;
import com.siam.mpl.Entities.Question;
import com.siam.mpl.Entities.TeamQuestion;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Repositories.QuestionDao;
import com.siam.mpl.Repositories.TeamDao;
import com.siam.mpl.Repositories.TeamQuestionDao;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
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

    //method to get the main question for each team from db
    @Transactional
    public Question getQuestion(QuestionDto questionDto) {
        if(questionDto.getTeamName() == null) {
            throw new IllegalArgumentException("Team name is empty!");
        }
        if(questionDto.getQuestionId() == null) {
            throw new IllegalArgumentException("Question id is empty!");
        }

        Optional<Question> optionalQuestion = questionDao.findById(questionDto.getQuestionId());

        if(optionalQuestion.isEmpty()) {
            throw new RuntimeException("No question with the given id found!");
        }

        Question question = optionalQuestion.get();
        String teamName = questionDto.getTeamName();

        saveTeamAndAssignQuestion(teamName, question);

        return question;
    }

    //method to save team in db and assign them a question
    public void saveTeamAndAssignQuestion(String teamName, Question question) {
        Optional<Teams> optionalTeam = teamDao.findByTeamName(teamName);

        if(optionalTeam.isPresent()) {
            throw new RuntimeException("Team with this name already exists!");
        }

        Optional<TeamQuestion> optionalTeamQuestion = teamQuestionDao.findByQuestionId(question.getId());

        if(optionalTeamQuestion.isPresent()) {
            throw new RuntimeException("This question is already allotted to some team");
        }

        //create new team and save in db
        Teams newTeam = new Teams();
        newTeam.setTeamName(teamName);
        newTeam.setPoints(100);
        newTeam.setEndTime(LocalDateTime.now().plus(Duration.ofMinutes(30)));

        //check for concurrent transactions passing the initial check
        //unique constraint acting as last line of defence
        try {
            teamDao.save(newTeam);
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
        } catch(DataIntegrityViolationException exception) {
            throw new RuntimeException("This question is already allotted to a team!");
        }
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
}