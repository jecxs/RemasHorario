package com.pontificia.remashorario.modules.teachingHour;

import com.pontificia.remashorario.modules.TimeSlot.TimeSlotEntity;
import com.pontificia.remashorario.modules.TimeSlot.TimeSlotService;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourRequestDTO;
import com.pontificia.remashorario.modules.teachingHour.dto.TeachingHourResponseDTO;
import com.pontificia.remashorario.modules.teachingHour.mapper.TeachingHourMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeachingHourService {

}
