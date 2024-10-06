package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import roomescape.global.auth.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> list() {
        return reservationService.findAll();
    }

    @PostMapping("/reservations")
    public ResponseEntity createReservation(
        @LoginMember Member loginMember,
        @RequestBody ReservationRequest reservationRequest
    ) {
        // if (reservationRequest.getDate() == null
        //         || reservationRequest.getTheme() == null
        //         || reservationRequest.getTime() == null) {
        //     return ResponseEntity.badRequest().build();
        // }
        //
        // if(reservationRequest.getName() == null) {
        //     reservationRequest.addName(loginMember.getName());
        // }

        reservationRequest.validate(loginMember.getName());

        reservationRequest.addMember(loginMember);

        if(reservationService.checkReservationExist(reservationRequest)) {
            return ResponseEntity.badRequest().build();
        }

        ReservationResponse reservation = reservationService.saveReservation(reservationRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }

    @PostMapping("/waitings")
    public ResponseEntity createWaiting(
        @LoginMember Member member,
        @RequestBody WaitingRequest waitingRequest
    ) {
        if(reservationService.checkWaitingExist(waitingRequest, member)) {
            return ResponseEntity.badRequest().build();
        }

        WaitingResponse waiting = reservationService.saveWaiting(waitingRequest, member);

        return ResponseEntity.created(URI.create("/waitings/" + waiting.id())).body(waiting);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity deleteWaiting(@PathVariable Long id) {
        reservationService.deleteWaitingById(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> getMemberReservations(
        @LoginMember Member loginMember
    ) {
        List<MyReservationResponse> response = reservationService.findReservationsByMember(loginMember.getId());
        return ResponseEntity.ok().body(response);
    }
}