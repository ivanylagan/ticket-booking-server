# Ticket Booking REST API server

Backend service for booking tickets

## Install

    ./mvnw clean install

## Run the app

    ./mvnw spring-boot:run


# REST API

The REST API to the example app is described below.

## Create new event

### Request

`POST /api/v1/admin/events`

    curl --location 'http://localhost:8080/api/v1/admin/events' \
    --header 'Content-Type: application/json' \
    --data '{
    "event_number": 123123,
    "row_count": 5,
    "seats_per_row": 5,
    "cancellation_window": 1
    }'

## Get available seats

### Request

`GET /api/v1/events/{event_number}/available-seats`

    curl --location 'http://localhost:8080/api/v1/events/1/available-seats'

## Get event details

### Request

`GET /api/v1/admin/events/{event_number}/details`

    curl --location 'http://localhost:8080/api/v1/admin/events/1/details'

## Book tickets for an event

### Request

`POST /api/v1/events/{event_number}/book`

    curl --location 'http://localhost:8080/api/v1/events/1/book' \
    --header 'Content-Type: application/json' \
    --data '{
    "seats": [
    "A1","A2"
    ],
    "phone_number": "234289321"
    }'

## Cancel ticket

### Request

`DELETE /api/v1/events/cancel`

    curl --location --request DELETE 'http://localhost:8080/api/v1/events/cancel' \
    --header 'Content-Type: application/json' \
    --data '{
    "ticket_number": "12341A1",
    "phone_number": "234289321"
    }'