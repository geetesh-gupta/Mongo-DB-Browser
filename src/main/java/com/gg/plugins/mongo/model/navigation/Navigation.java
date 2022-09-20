/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model.navigation;

import com.gg.plugins.mongo.model.MongoCollection;
import com.gg.plugins.mongo.model.MongoQueryOptions;

import java.util.ArrayList;
import java.util.List;

public class Navigation {

	private final List<WayPoint> wayPoints = new ArrayList<>();

	private WayPoint currentWayPoint = null;

	public void addNewWayPoint(MongoCollection collection, MongoQueryOptions mongoQueryOptions) {
		currentWayPoint = new WayPoint(collection, mongoQueryOptions);
		wayPoints.add(currentWayPoint);
	}

	public WayPoint getCurrentWayPoint() {
		return currentWayPoint;
	}

	public List<WayPoint> getWayPoints() {
		return wayPoints;
	}

	public void moveBackward() {
		if (currentWayPoint != null) {
			int currentWayPointIndex = wayPoints.indexOf(currentWayPoint);
			if (currentWayPointIndex > 0) {
				currentWayPoint = wayPoints.get(currentWayPointIndex - 1);
				wayPoints.remove(currentWayPointIndex);
			}
		}
	}

	public static class WayPoint {
		private final MongoCollection collection;

		private MongoQueryOptions queryOptions;

		WayPoint(MongoCollection collection, MongoQueryOptions queryOptions) {
			this.collection = collection;
			this.queryOptions = queryOptions;
		}

		public String getLabel() {
			return collection.getParentDatabase().getName() + "/" + collection.getName();
		}

		public MongoCollection getCollection() {
			return collection;
		}

		public MongoQueryOptions getQueryOptions() {
			return queryOptions;
		}

		public void setQueryOptions(MongoQueryOptions queryOptions) {
			this.queryOptions = queryOptions;
		}
	}
}
