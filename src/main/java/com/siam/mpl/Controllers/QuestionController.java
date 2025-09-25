package com.siam.mpl.Controllers;

import com.siam.mpl.DTOs.NewQuestionDto;
import com.siam.mpl.DTOs.QuestionDto;
import com.siam.mpl.DTOs.QuestionResponseDto;
import com.siam.mpl.DTOs.QuestionUpdateDto;
import com.siam.mpl.Entities.Question;
import com.siam.mpl.Services.QuestionService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QuestionController {
    //fields
    QuestionService questionService;

    //dependency injection
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    //method to receive request to get initial question from db
    @PostMapping(value="/question/get")
    public ResponseEntity<Question> getQuestion(@Valid @RequestBody QuestionDto questionDto) {
        return new ResponseEntity<>(questionService.tryToGetQuestion(questionDto), HttpStatus.OK);
    }

    //method to remove a question from db
    @DeleteMapping(value="/question/remove/{id}")
    public ResponseEntity<Question> removeQuestion(@PathVariable String id) {
        return new ResponseEntity<>(questionService.removeQuestion(id), HttpStatus.OK);
    }

    //method to add a question
    @PostMapping(value="/question/add")
    public ResponseEntity<Question> addQuestion(@Valid @RequestBody NewQuestionDto newQuestionDto) {
        return new ResponseEntity<>(questionService.addQuestion(newQuestionDto), HttpStatus.OK);
    }

    //method to update a question by id
    @PatchMapping(value="/question/update/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id,@Valid @RequestBody QuestionUpdateDto questionUpdateDto) {
        return new ResponseEntity<>(questionService.updateQuestion(id, questionUpdateDto), HttpStatus.OK);
    }

    //method to handle main question submission
    @PostMapping(value="/question/result")
    public ResponseEntity<Void> questionSubmit(@Valid @RequestBody QuestionResponseDto questionResponseDto) {
        questionService.handleQuestionSubmission(questionResponseDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
