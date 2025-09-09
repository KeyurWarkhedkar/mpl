package com.siam.mpl.Controllers;

import com.siam.mpl.DTOs.QuestionDto;
import com.siam.mpl.Entities.Question;
import com.siam.mpl.Services.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {
    //fields
    QuestionService questionService;

    //dependency injection
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    //method to receive request to get initial question from db
    @PostMapping(value="/getQuestion")
    public ResponseEntity<Question> getQuestion(@RequestBody QuestionDto questionDto) {
        return new ResponseEntity<>(questionService.getQuestion(questionDto), HttpStatus.OK);
    }
}
