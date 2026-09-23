import { apiClient } from "./client";

export const getTrips = (userId) =>
	apiClient.get("/api/trips", { params: { userId } }).then((res) => res.data);

export const getTrip = (id) =>
	apiClient.get(`/api/trips/${id}`).then((res) => res.data);

export const createTrip = (request) =>
	apiClient.post("/api/trips", request).then((res) => res.data);
