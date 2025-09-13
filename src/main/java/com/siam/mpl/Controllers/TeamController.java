package com.siam.mpl.Controllers;

import com.siam.mpl.DTOs.TeamUpdateDto;
import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Services.TeamService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TeamController {
    //fields
    TeamService teamService;

    //dependency injection
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    //method to receive a request to delete a team
    @DeleteMapping(value="/team/remove/{id}")
    public ResponseEntity<String> removeTeam(@PathVariable int id) {
        return new ResponseEntity<>(teamService.removeTeam(id), HttpStatus.OK);
    }

    //method to update team details
    @PatchMapping(value="/team/update/{id}")
    public ResponseEntity<Teams> updateTeam(@PathVariable int id, @RequestBody TeamUpdateDto teamUpdateDto) {
        return new ResponseEntity<>(teamService.updateTeamDetails(id, teamUpdateDto), HttpStatus.OK);
    }

    //method to get a team by id
    @GetMapping(value="/team/getById/{id}")
    public ResponseEntity<Teams> getTeamById(@PathVariable int id) {
        return new ResponseEntity<>(teamService.getTeamById(id), HttpStatus.OK);
    }

    //method to get a team by name
    @GetMapping(value="/team/getByTeamName/{teamName}")
    public ResponseEntity<Teams> getTeamByTeamName(@PathVariable String teamName) {
        return new ResponseEntity<>(teamService.getTeamByTeamName(teamName), HttpStatus.OK);
    }

    //method to get all teams in the db
    @GetMapping(value="/team/getALl")
    public ResponseEntity<List<Teams>> getAllTeams() {
        return new ResponseEntity<>(teamService.getAllTeams(), HttpStatus.OK);
    }
}
