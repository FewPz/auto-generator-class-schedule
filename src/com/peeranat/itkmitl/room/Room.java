package com.peeranat.itkmitl.room;

public class Room {

	private String roomNumber;
	private RoomType roomType;

	public Room(String roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
    }

	public String getRoomNumber() {
		return roomNumber;
	}
	
	public RoomType getRoomType() {
		return roomType;
	}

}
