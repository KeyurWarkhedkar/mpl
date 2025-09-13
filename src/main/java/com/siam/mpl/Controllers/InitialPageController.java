package com.siam.mpl.Controllers;

import com.siam.mpl.Entities.Teams;
import com.siam.mpl.Repositories.TeamDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

//controller class to return the initial html pages
@Controller
public class InitialPageController {
    //fields
    TeamDao teamDao;

    //dependency injection
    public InitialPageController(TeamDao teamDao) {
        this.teamDao = teamDao;
    }

    //method to return the admin page
    @GetMapping(value="/admin/get")
    public String getAdminPage(Model model) {
        List<Teams> teams = teamDao.findAll();
        model.addAttribute("teams", teams);
        return "Admin";
    }
}
