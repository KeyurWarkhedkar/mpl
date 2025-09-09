package com.siam.mpl.Controllers;

import com.siam.mpl.DTOs.MysteryBoxDto;
import com.siam.mpl.Entities.MysteryQuestion;
import com.siam.mpl.Services.MysteryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MysteryBoxController {
    //fields
    MysteryService mysteryService;

    //dependency injection
    public MysteryBoxController(MysteryService mysteryService) {
        this.mysteryService = mysteryService;
    }

    //method to receive request to display mystery box and question and reduce points accordingly
    @PostMapping(value="/getMysteryQuestion")
    public ResponseEntity<MysteryQuestion> getMysteryQuestion(@RequestBody MysteryBoxDto mysteryBoxDto) {
        return new ResponseEntity<>(mysteryService.assignMysteryQuestion(mysteryBoxDto), HttpStatus.OK);
    }

    //method to quit mystery question
}