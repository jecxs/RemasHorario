package com.pontificia.remashorario.modules.teachingHour;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeachingHourRepository extends JpaRepository<TeachingHourEntity, UUID> {
    /**
     * Obtiene todas las horas pedagógicas ordenadas por el inicio de su turno y por
     * su posición dentro del turno. Se eliminó la condición por "isActive" ya que el
     * atributo no existe en la entidad.
     */
    @Query("SELECT th FROM TeachingHourEntity th ORDER BY th.timeSlot.startTime ASC, th.orderInTimeSlot ASC")
    List<TeachingHourEntity> findAllOrderByTimeSlotAndOrder();

    /**
     * Obtiene las horas pedagógicas de un turno concreto ordenadas por su posición.
     * Se eliminó la condición por "isActive" ya que el atributo no existe en la entidad.
     */
    List<TeachingHourEntity> findByTimeSlotOrderByOrderInTimeSlot(TimeSlotEntity timeSlot);


}
