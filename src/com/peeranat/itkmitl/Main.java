package com.peeranat.itkmitl;

import com.peeranat.itkmitl.scheduler.SchedulerHandler;

public class Main {
	
	public static void main(String[] args) {
		SchedulerHandler schedulerHandler = new SchedulerHandler();
		schedulerHandler.initializeData();
		schedulerHandler.generateSchedule();
		schedulerHandler.printSchedule();
	}

}
