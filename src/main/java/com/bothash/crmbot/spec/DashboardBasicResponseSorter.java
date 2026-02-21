package com.bothash.crmbot.spec;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bothash.crmbot.dto.DashboardBasicResponse;

public class DashboardBasicResponseSorter {

    // Generic method to sort the list based on a given field
    public static void sortDashboardList(List<DashboardBasicResponse> list, String sortBy, boolean ascending) {
        Comparator<DashboardBasicResponse> comparator;

        switch (sortBy.toLowerCase()) {
            case "username":
                comparator = Comparator.comparing(DashboardBasicResponse::getUserName, Comparator.nullsLast(String::compareToIgnoreCase));
                break;
            case "todaysscheduled":
                comparator = Comparator.comparing(DashboardBasicResponse::getTodaysScheduled, Comparator.nullsLast(Integer::compareTo));
                break;
            case "missedschedule":
                comparator = Comparator.comparing(DashboardBasicResponse::getMissedSchedule, Comparator.nullsLast(Integer::compareTo));
                break;
            case "counselled":
                comparator = Comparator.comparing(DashboardBasicResponse::getCounselled, Comparator.nullsLast(Integer::compareTo));
                break;
            case "converted":
                comparator = Comparator.comparing(DashboardBasicResponse::getConverted, Comparator.nullsLast(Integer::compareTo));
                break;
            case "total":
                comparator = Comparator.comparing(DashboardBasicResponse::getTotal, Comparator.nullsLast(Integer::compareTo));
                break;
            case "dustbin":
                comparator = Comparator.comparing(DashboardBasicResponse::getDustbin, Comparator.nullsLast(Integer::compareTo));
                break;
            case "blank":
                comparator = Comparator.comparing(DashboardBasicResponse::getBlank, Comparator.nullsLast(Integer::compareTo));
            case "hot":
                comparator = Comparator.comparing(DashboardBasicResponse::getHot, Comparator.nullsLast(Integer::compareTo));
                break;
            case "prospect":
                comparator = Comparator.comparing(DashboardBasicResponse::getProspect, Comparator.nullsLast(Integer::compareTo));
                break;
            case "cold":
                comparator = Comparator.comparing(DashboardBasicResponse::getCold, Comparator.nullsLast(Integer::compareTo));
                break;
            case "nocomment":
                comparator = Comparator.comparing(DashboardBasicResponse::getNoComment, Comparator.nullsLast(Integer::compareTo));
                break;
            case "noschedule":
                comparator = Comparator.comparing(DashboardBasicResponse::getNoSchedule, Comparator.nullsLast(Integer::compareTo));
                break;
            default:
                throw new IllegalArgumentException("Invalid sort parameter: " + sortBy);
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(list, comparator);
    }
}
