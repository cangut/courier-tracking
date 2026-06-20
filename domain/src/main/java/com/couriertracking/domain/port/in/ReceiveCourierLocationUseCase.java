package com.couriertracking.domain.port.in;

public interface ReceiveCourierLocationUseCase {

    void receive(ReceiveCourierLocationCommand command);
}
