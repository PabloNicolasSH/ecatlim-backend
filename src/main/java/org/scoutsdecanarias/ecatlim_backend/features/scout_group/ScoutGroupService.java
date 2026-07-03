package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

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

    public ScoutGroup create(ScoutGroupDto scoutGroup) {
        ScoutGroup scoutGroupEntity = new ScoutGroup();
        scoutGroupEntity.setName(scoutGroup.name());
        scoutGroupEntity.setProvinceId(scoutGroup.provinceId());
        scoutGroupEntity.setGroupNumber(scoutGroup.groupNumber());
        scoutGroupEntity.setEmail(scoutGroup.email());
        return scoutGroupRepository.save(scoutGroupEntity);
    }

    public ScoutGroup update(Integer id, ScoutGroupDto scoutGroup) {
        ScoutGroup scoutGroupEntity = getScoutGroupById(id);
        scoutGroupEntity.setName(scoutGroup.name());
        scoutGroupEntity.setProvinceId(scoutGroup.provinceId());
        scoutGroupEntity.setGroupNumber(scoutGroup.groupNumber());
        scoutGroupEntity.setEmail(scoutGroup.email());
        return scoutGroupRepository.save(scoutGroupEntity);
    }

    public void delete(Integer id) {
        scoutGroupRepository.deleteById(id);
    }
}
