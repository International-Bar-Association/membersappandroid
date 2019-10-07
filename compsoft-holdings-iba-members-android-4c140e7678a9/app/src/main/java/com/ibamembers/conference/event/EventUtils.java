package com.ibamembers.conference.event;


import android.content.Context;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EventUtils {

	private static final String ROOM_JSON = "roominfo.json";

	public static Room getRoomFromJSON(Context context, int roomId) {
		String json = loadJSONFromAsset(context, ROOM_JSON);
		RoomList roomList= new Gson().fromJson(json, RoomList.class);
		if (roomList != null) {
			return roomList.getRooms().get(roomId);
		}
		return  null;
	}

	private static String loadJSONFromAsset(Context context, String jsonName) {
		String json;

		try {
			InputStream is = context.getAssets().open(jsonName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	public class RoomList {
		@SuppressWarnings("unused")
		@SerializedName("rooms")
		private List<Room> rooms;

		public SparseArray<Room> getRooms() {
			SparseArray<Room> roomMap = new SparseArray<>();
			for (Room room : rooms) {
				roomMap.append(room.getRoomId(), room);
			}
			return roomMap;
		}
	}

	public class Room {
		@SuppressWarnings("unused")
		@SerializedName("roomId")
		private int roomId;

		@SuppressWarnings("unused")
		@SerializedName("roomName")
		private String roomName;

		@SuppressWarnings("unused")
		@SerializedName("buildingName")
		private String buildingName;

		@SuppressWarnings("unused")
		@SerializedName("level")
		private int level;

		@SuppressWarnings("unused")
		@SerializedName("location")
		private Location location;

		public Room(int roomId, String roomName, String buildingName) {
			this.roomId = roomId;
			this.roomName = roomName;
			this.buildingName = buildingName;
		}

		public int getRoomId() {
			return roomId;
		}

		public String getRoomName() {
			return roomName;
		}

		public String getBuildingName() {
			return buildingName;
		}

		public int getLevel() {
			return level;
		}

		public Location getLocation() {
			return location;
		}
	}

	public class Location {
		@SuppressWarnings("unused")
		@SerializedName("x")
		private int x;

		@SuppressWarnings("unused")
		@SerializedName("y")
		private int y;

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
}
