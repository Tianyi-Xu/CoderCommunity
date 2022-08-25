package com.tianyi.community.controller;

import com.tianyi.community.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // also accept post request, allow being able to be forward to
    @RequestMapping(path="/statistics", method={RequestMethod.GET, RequestMethod.POST})
    public String getStatsPage() {
        return "/site/admin/stats";
    }

    @RequestMapping(path="/statistics/uv", method=RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern="yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern="yyyy-MM-dd") Date end, Model model) {
        long uv = statisticsService.countUV(start, end);
        model.addAttribute("UVResult", uv);
        model.addAttribute("UVStartDate", start);
        model.addAttribute("UVEndDate", end);

        return "forward:/statistics";
    }

    @RequestMapping(path="/statistics/dau", method=RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern="yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern="yyyy-MM-dd") Date end, Model model) {
        long dau = statisticsService.countDAU(start, end);
        model.addAttribute("DAUResult", dau);
        model.addAttribute("DAUStartDate", start);
        model.addAttribute("DAUEndDate", end);

        return "forward:/statistics";
    }


}
