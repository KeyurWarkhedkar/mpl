package com.siam.mpl.Controllers;

import com.siam.mpl.DTOs.MysteryBoxDto;
import com.siam.mpl.DTOs.MysteryCompletionDto;
import com.siam.mpl.DTOs.NewMysteryDto;
import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Enums.MysteryStatus;
import com.siam.mpl.Services.MysteryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MysteryBoxController {
    //fields
    MysteryService mysteryService;

    //dependency injection
    public MysteryBoxController(MysteryService mysteryService) {
        this.mysteryService = mysteryService;
    }

    //method to receive request to display mystery box and question and reduce points accordingly
    @PostMapping(value="/mysteryQuestion/get")
    public ResponseEntity<MysteryQuestion> getMysteryQuestion(@RequestBody MysteryBoxDto mysteryBoxDto) {
        return new ResponseEntity<>(mysteryService.assignMysteryQuestion(mysteryBoxDto), HttpStatus.OK);
    }

    //method to handle mystery question result
    @PostMapping(value="/mysteryQuestion/result")
    public ResponseEntity<String> handleMysteryResult(@RequestBody MysteryCompletionDto mysteryCompletionDto) {
        if(mysteryCompletionDto.getMysteryCompletionStatus().equals(MysteryStatus.DONE)) {
            return new ResponseEntity<>(mysteryService.mysterySuccessHandle(mysteryCompletionDto), HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(mysteryService.quitMysteryQuestion(mysteryCompletionDto), HttpStatus.ACCEPTED);
        }
    }

    //method to add a mystery question
    @PostMapping(value="/mysteryQuestion/add")
    public ResponseEntity<MysteryQuestion> addMysteryQuestion(@RequestBody NewMysteryDto newMysteryDto) {
        return new ResponseEntity<>(mysteryService.addMysteryQuestion(newMysteryDto), HttpStatus.OK);
    }

    //method to remove a mystery question
    @DeleteMapping(value="/mysteryQuestion/remove/{id}")
    public ResponseEntity<MysteryQuestion> removeMysteryQuestion(@PathVariable int id) {
        return new ResponseEntity<>(mysteryService.removeMysteryQuestion(id), HttpStatus.OK);
    }
}