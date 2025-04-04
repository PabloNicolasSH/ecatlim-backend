package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.repository.ScoutGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoutGroupService {

    private final ScoutGroupRepository scoutGroupRepository;

    public ScoutGroupService(ScoutGroupRepository scoutGroupRepository) {
        this.scoutGroupRepository = scoutGroupRepository;
    }

    public List<ScoutGroup> getScoutGroups() {
        return scoutGroupRepository.findAll();
    }

    public List<ScoutGroup> getScoutGroupsByProvinceId(int provinceId) {
        return scoutGroupRepository.findAllByProvinceId(provinceId);
    }

    public ScoutGroup getScoutGroupById(int id) {
        return scoutGroupRepository.findById(id).orElseThrow();
    }
}
