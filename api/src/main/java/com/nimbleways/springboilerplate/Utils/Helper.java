package com.nimbleways.springboilerplate.Utils;

import java.time.LocalDate;

public class Helper {


    public static boolean isWithinSeason(LocalDate startDate, LocalDate endDate) {
        return  (LocalDate.now().isAfter(startDate) && LocalDate.now().isBefore(endDate));
    }

}
