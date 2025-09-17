package com.siam.mpl.Services;

import com.siam.mpl.DTOs.MysteryBoxDto;
import com.siam.mpl.DTOs.MysteryCompletionDto;
import com.siam.mpl.DTOs.NewMysteryDto;
import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Enums.QuestionStatus;
import com.siam.mpl.Repositories.MysteryQuestionDao;
import com.siam.mpl.Repositories.TeamDao;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MysteryService {
    //fields
    TeamDao teamDao;
    MysteryQuestionDao mysteryQuestionDao;
    SimpMessagingTemplate simpMessagingTemplate;

    //dependency injection
    public MysteryService(TeamDao teamDao, MysteryQuestionDao mysteryQuestionDao, SimpMessagingTemplate simpMessagingTemplate) {
        this.teamDao = teamDao;
        this.mysteryQuestionDao = mysteryQuestionDao;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //method to return mystery question from db and assign to a team
    @Transactional
    public MysteryQuestion assignMysteryQuestion(MysteryBoxDto mysteryBoxDto) {
        //find the team from db
        Optional<Teams> optionalTeam = teamDao.findByTeamName(mysteryBoxDto.getTeamName());

        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with such name exists!");
        }

        Teams team = optionalTeam.get();
        //check if the team has already a question allotted to them
        if(team.getMysteryQuestion() == null) {
            List<MysteryQuestion> mysteryQuestions = mysteryQuestionDao.findByDifficultyAndQuestionStatus(mysteryBoxDto.getDifficulty(), QuestionStatus.UNALLOCATED);

            if(mysteryQuestions.isEmpty()) {
                throw new RuntimeException("No questions available!");
            }

            MysteryQuestion mysteryQuestionToBeAllocated = mysteryQuestions.getFirst();

            //assign the question and set its status to ALLOCATED and save updated values to db
            team.setMysteryQuestion(mysteryQuestionToBeAllocated);
            team.setPoints(team.getPoints() - mysteryBoxDto.getPointsDeducted());
            try {
                teamDao.save(team);
            } catch(DataIntegrityViolationException ex) {
                throw new RuntimeException("A team with this id already exists!");
            }

            mysteryQuestionToBeAllocated.setQuestionStatus(QuestionStatus.ALLOCATED);

            try {
                mysteryQuestionDao.save(mysteryQuestionToBeAllocated);
            } catch(DataIntegrityViolationException ex) {
                return team.getMysteryQuestion();
            }

            return mysteryQuestionToBeAllocated;
        } else {
            //if the team already has a mystery question allotted then return the same question
            return team.getMysteryQuestion();
        }
    }

    //method to quit a mystery question assigned to a team
    @Transactional
    public String quitMysteryQuestion(MysteryCompletionDto mysteryCompletionDto) {
        Optional<Teams> optionalTeam = teamDao.findByTeamName(mysteryCompletionDto.getTeamName());

        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given name found!");
        }

        Teams quittingTeam = optionalTeam.get();

        if(quittingTeam.getMysteryQuestion() == null) {
            throw new RuntimeException("There was no mystery question allotted to the team!");
        }

        //set the mystery question of the team to null and save updated values to the db
        quittingTeam.setMysteryQuestion(null);
        teamDao.save(quittingTeam);

        return "Question skipped successfully!";
    }

    //method to add bonus time to the team whose mystery question is solved
    @Transactional
    public String mysterySuccessHandle(MysteryCompletionDto mysteryCompletionDto) {
        //get the team whose timer need to be updated
        Optional<Teams> optionalTeam = teamDao.findByTeamName(mysteryCompletionDto.getTeamName());

        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given name found!");
        }

        Teams mysteryCompletedTeam = optionalTeam.get();

        //set the mystery question of the team to be null so that the team can bid for new
        //mystery questions.
        mysteryCompletedTeam.setMysteryQuestion(null);

        //calculate the updated remaining time for the team
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = mysteryCompletedTeam.getEndTime();
        Duration timeRemaining = Duration.between(now, endTime);
        if(timeRemaining.isNegative()) {
            throw new RuntimeException("You have exhausted your time for the main question!");
        }

        //also update the end time for the team for successful handle of next bonus updates before saving
        mysteryCompletedTeam.setEndTime(endTime.plus(Duration.ofMinutes(5)));

        //get the new remaining time for the team
        Duration updatedTime = Duration.between(now, mysteryCompletedTeam.getEndTime());

        //send the updated time to the appropriate client
        String destination = "/topic/time/" + mysteryCompletionDto.getTeamName().replace(" ","");
        simpMessagingTemplate.convertAndSend(destination, updatedTime);

        teamDao.save(mysteryCompletedTeam);

        return "Congratulations! Your team just earned some bonus time.";
    }

    //method to add a mystery question
    @Transactional
    public MysteryQuestion addMysteryQuestion(NewMysteryDto newMysteryDto) {
        MysteryQuestion mysteryQuestion = new MysteryQuestion();
        mysteryQuestion.setQuestion(newMysteryDto.getQuestion());
        mysteryQuestion.setDifficulty(newMysteryDto.getDifficulty());
        mysteryQuestion.setQuestionStatus(QuestionStatus.UNALLOCATED);
        return mysteryQuestionDao.save(mysteryQuestion);
    }

    //method to remove a mystery question
    @Transactional
    public MysteryQuestion removeMysteryQuestion(int questionId) {
        Optional<MysteryQuestion> optionalMysteryQuestion = mysteryQuestionDao.findById(questionId);
        if(optionalMysteryQuestion.isEmpty()) {
            throw new RuntimeException("No question with the given id exists!");
        }

        MysteryQuestion questionToBeRemoved = optionalMysteryQuestion.get();

        if(questionToBeRemoved.getQuestionStatus().equals(QuestionStatus.ALLOCATED)) {
            throw new RuntimeException("This question is already allotted to a team!");
        }

        mysteryQuestionDao.delete(questionToBeRemoved);

        return questionToBeRemoved;
    }

    //method to get all mystery questions
    @Transactional
    public List<MysteryQuestion> getAllMysteryQuestion() {
        List<MysteryQuestion> mysteryQuestions = mysteryQuestionDao.findAll();
        if(mysteryQuestions.isEmpty()) {
            throw new RuntimeException("No mystery questions found!");
        } else {
            return mysteryQuestions;
        }
    }
}
