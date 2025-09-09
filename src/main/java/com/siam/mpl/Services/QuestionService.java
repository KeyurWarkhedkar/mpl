package com.siam.mpl.Services;

import com.siam.mpl.DTOs.QuestionDto;
import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Entities.Question;
import com.siam.mpl.Entities.TeamQuestion;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Repositories.QuestionDao;
import com.siam.mpl.Repositories.TeamDao;
import com.siam.mpl.Repositories.TeamQuestionDao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        //create new team and save in db
        Teams newTeam = new Teams();
        newTeam.setTeamName(teamName);
        newTeam.setPoints(100);
        newTeam.setStartTime(LocalDateTime.now());
        teamDao.save(newTeam);

        //assign the new team a question
        TeamQuestion newTeamQuestion = new TeamQuestion();
        newTeamQuestion.setTeam(newTeam);
        newTeamQuestion.setQuestion(question);
        teamQuestionDao.save(newTeamQuestion);
    }
}