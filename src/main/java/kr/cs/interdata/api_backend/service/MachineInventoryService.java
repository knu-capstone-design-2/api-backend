package kr.cs.interdata.api_backend.service;

import kr.cs.interdata.api_backend.entity.MonitoredMachineInventory;
import kr.cs.interdata.api_backend.entity.TargetType;
import kr.cs.interdata.api_backend.repository.MonitoredMachineInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineInventoryService {

    private final Logger logger = LoggerFactory.getLogger(MonitoringDefinitionService.class);

    public final MonitoredMachineInventoryRepository monitoredMachineInventoryRepository;

    @Autowired
    public MachineInventoryService(MonitoredMachineInventoryRepository monitoredMachineInventoryRepository) {
        this.monitoredMachineInventoryRepository = monitoredMachineInventoryRepository;
    }

    // type당 고유 id를 순서대로 생성하는 메서드(ex. host001, container002)
    public String generateNextId(String type) {
        Pageable topOne = PageRequest.of(0, 1); // 가장 최근 하나만 가져옴
        List<String> topIds = monitoredMachineInventoryRepository.findTopIdByType(type, topOne);

        int nextNumber = 1;
        if (!topIds.isEmpty()) {
            String lastId = topIds.get(0); // 예: host003
            String numberPart = lastId.replace(type, ""); // "003"
            try {
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                throw new RuntimeException("id 형식이 잘못되었습니다(ex. host013, container002): " + lastId);
            }
        }

        return String.format("%s%03d", type, nextNumber); // host004
    }

    // 머신 등록 - add
    public void addMachine(String type, String machine_id) {
        TargetType targetType = new TargetType();
        targetType.setType(type);

        MonitoredMachineInventory machine = new MonitoredMachineInventory();
        String id = generateNextId(type);
        machine.setId(id);
        machine.setType(targetType);
        machine.setMachineId(machine_id);

        monitoredMachineInventoryRepository.save(machine);

        logger.info("머신을 성공적으로 등록하였습니다: {} -> {}, {}", id, type, machine_id);
    }

    // 머신 모든 숫자 조회
    public int retrieveAllMachineNumber() {
        int result = 0;
        result = monitoredMachineInventoryRepository.countAll();

        return result;
    }

    // "type"을 입력한 타입의 머신 총 숫자 조회
    public int retrieveMachineNumberByType(String type) {
        int result = 0;
        result = monitoredMachineInventoryRepository.countByType(type);

        return result;
    }
}
