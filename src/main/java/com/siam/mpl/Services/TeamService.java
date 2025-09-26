package com.siam.mpl.Services;

import com.siam.mpl.DTOs.MysteryCompletionResponseDto;
import com.siam.mpl.DTOs.TeamUpdateDto;
import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Entities.TeamQuestion;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Enums.QuestionStatus;
import com.siam.mpl.Repositories.MysteryQuestionDao;
import com.siam.mpl.Repositories.TeamDao;
import com.siam.mpl.Repositories.TeamQuestionDao;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TeamService {
    //fields
    TeamDao teamDao;
    TeamQuestionDao teamQuestionDao;
    MysteryQuestionDao mysteryQuestionDao;
    SimpMessagingTemplate simpMessagingTemplate;

    //dependency injection
    public TeamService(TeamDao teamDao, TeamQuestionDao teamQuestionDao, MysteryQuestionDao mysteryQuestionDao, SimpMessagingTemplate simpMessagingTemplate) {
        this.teamDao = teamDao;
        this.teamQuestionDao = teamQuestionDao;
        this.mysteryQuestionDao = mysteryQuestionDao;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    //method to delete a team from the database
    @Transactional
    public String removeTeam(int teamId) {
        //check if the team to be deleted exists in the db
        Optional<Teams> optionalTeam = teamDao.findById(teamId);

        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given id found!");
        }

        Teams teamToBeDeleted = optionalTeam.get();

        //delete the mapping of the team to the question before deleting the team
        Optional<TeamQuestion> optionalTeamQuestion = teamQuestionDao.findByTeamId(teamId);
        teamQuestionDao.delete(optionalTeamQuestion.get());

        //delete the team from the db
        teamDao.delete(teamToBeDeleted);

        return "Team deleted successfully!";
    }

    //method to update the team details
    @Transactional
    public Teams updateTeamDetails(int id, TeamUpdateDto teamUpdateDto) {
        log.info("Request for update for team {} from admin received", teamUpdateDto.getTeamName());
        Optional<Teams> optional = teamDao.findById(id);

        if(optional.isEmpty()) {
            log.error("Team {} is not registered. Cannot update team details", teamUpdateDto.getTeamName());
            throw new RuntimeException("No team with the given id found!");
        }

        Teams teamForUpdate = optional.get();

        //update the details which are not null
        if(teamUpdateDto.getTeamName() != null) {
            log.info("Team name updated successfully");
            teamForUpdate.setTeamName(teamUpdateDto.getTeamName());
        }
        if(teamUpdateDto.getPoints() != null) {
            teamForUpdate.setPoints(teamUpdateDto.getPoints());

            //send the updated time to the appropriate client
            String destination = "/topic/time/" + teamForUpdate.getTeamName().replace(" ","");
            simpMessagingTemplate.convertAndSend(destination, teamForUpdate.getPoints());

            log.info("Team points update successfully");
        }
        /*if(teamUpdateDto.getMysteryQuestionId() != null) {
            //check if there is any such mystery question in the db
            Optional<MysteryQuestion> optionalMysteryQuestion = mysteryQuestionDao.findById(teamUpdateDto.getMysteryQuestionId());

            if(optionalMysteryQuestion.isEmpty()) {
                throw new RuntimeException("No mystery question with the given id found!");
            }

            MysteryQuestion newMysteryQuestion = optionalMysteryQuestion.get();

            //check if the mystery question is already allocated to some team
            if(newMysteryQuestion.getQuestionStatus().equals(QuestionStatus.ALLOCATED)) {
                throw new RuntimeException("This mystery question is already allocated to some other team!");
            } else {
                //if it is not allocated then update the mystery question for the current
                //team and set the question as ALLOCATED
                teamForUpdate.setMysteryQuestion(newMysteryQuestion);
                newMysteryQuestion.setQuestionStatus(QuestionStatus.ALLOCATED);
            }
        }*/

        return teamDao.save(teamForUpdate);
    }

    //method to get a team by id
    @Transactional
    public Teams getTeamById(int teamId) {
        Optional<Teams> optionalTeam = teamDao.findById(teamId);
        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given is exists!");
        } else {
            return optionalTeam.get();
        }
    }

    //method to get a team by team name
    @Transactional
    public Teams getTeamByTeamName(String teamName) {
        Optional<Teams> optionalTeam = teamDao.findByTeamName(teamName);
        if(optionalTeam.isEmpty()) {
            throw new RuntimeException("No team with the given name exists!");
        } else {
            return optionalTeam.get();
        }
    }

    //method to get all teams in the database
    @Transactional
    public List<Teams> getAllTeams() {
        List<Teams> teams = teamDao.findAll();
        if(teams.isEmpty()) {
            throw new RuntimeException("No teams in the database!");
        } else {
            return teams;
        }
    }
}
