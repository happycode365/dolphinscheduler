/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_DELETE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkflowDefinitionCount;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class ProjectServiceImpl extends BaseServiceImpl implements ProjectService {

    @Lazy
    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 프로젝트를 생성합니다.
     *
     * @param loginUser 로그인 사용자
     * @param name      프로젝트 이름
     * @param desc      설명
     * @return 에러가 있는 경우 에러를 반환합니다.
     */
    @Override
    @Transactional
    public Result createProject(User loginUser, String name, String desc) {
        Result result = new Result();

        checkDesc(result, desc);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.PROJECTS, PROJECT_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Project project = projectMapper.queryByName(name);
        if (project != null) {
            log.warn("Project {} already exists.", project.getName());
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, name);
            return result;
        }

        Date now = new Date();

        project = Project
                .builder()
                .name(name)
                .code(CodeGenerateUtils.genCode())
                .description(desc)
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();

        if (projectMapper.insert(project) > 0) {
            log.info("Project is created and id is :{}", project.getId());
            result.setData(project);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project create error, projectName:{}.", project.getName());
            putMsg(result, Status.CREATE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * 프로젝트 설명을 확인합니다.
     *
     * @param result 결과 객체
     * @param desc   설명
     */
    public static void checkDesc(Result result, String desc) {
        if (!StringUtils.isEmpty(desc) && desc.codePointCount(0, desc.length()) > 255) {
            result.setCode(Status.DESCRIPTION_TOO_LONG_ERROR.getCode());
            result.setMsg(Status.DESCRIPTION_TOO_LONG_ERROR.getMsg());
        } else {
            result.setCode(Status.SUCCESS.getCode());
        }
    }

    /**
     * 코드로 프로젝트 상세 정보를 조회합니다.
     *
     * @param projectCode 프로젝트 코드
     * @return 프로젝트 상세 정보
     */
    @Override
    public Result queryByCode(User loginUser, long projectCode) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndPerm = hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }
        if (project != null) {
            result.setData(project);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    @Override
    public Map<String, Object> queryByName(User loginUser, String projectName) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        boolean hasProjectAndPerm = hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }
        if (project != null) {
            result.put(Constants.DATA_LIST, project);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * 프로젝트 및 권한을 확인합니다.
     *
     * @param loginUser   로그인 사용자
     * @param project     프로젝트
     * @param projectCode 프로젝트 코드
     * @param permission  권한
     * @return 로그인 사용자가 프로젝트를 볼 권한이 있는 경우 true가 포함된 결과 맵
     */
    @Override
    public Map<String, Object> checkProjectAndAuth(User loginUser, Project project, long projectCode,
                                                   String permission) {
        Map<String, Object> result = new HashMap<>();
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_EXIST);
        } else if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS,
                permission)) {
            // check read permission
            log.error("User does not have {} permission to operate project, userName:{}, projectCode:{}.",
                    permission, loginUser.getUserName(), projectCode);
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), projectCode);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    public void checkProjectAndAuthThrowException(@NonNull User loginUser, @Nullable Project project,
                                                  String permission) {
        // todo: throw a permission exception
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS, permission)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(),
                    project.getCode());
        }
    }

    @Override
    public void checkProjectAndAuthThrowException(User loginUser, Long projectCode, String permission) {
        if (projectCode == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        Project project = projectMapper.queryByCode(projectCode);
        checkProjectAndAuthThrowException(loginUser, project, permission);
    }

    @Override
    public boolean hasProjectAndPerm(User loginUser, Project project, Map<String, Object> result, String permission) {
        boolean checkResult = false;
        if (project == null) {
            log.error("Project does not exist.");
            putMsg(result, Status.PROJECT_NOT_FOUND, "");
        } else if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS,
                permission)) {
            log.error("User does not have {} permission to operate project, userName:{}, projectCode:{}.",
                    permission, loginUser.getUserName(), project.getCode());
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), project.getCode());
        } else {
            checkResult = true;
        }
        return checkResult;
    }

    @Override
    public boolean hasProjectAndWritePerm(User loginUser, Project project, Result result) {
        boolean checkResult = false;
        if (project == null) {
            log.error("Project does not exist.");
            putMsg(result, Status.PROJECT_NOT_FOUND, "");
        } else {
            // case 1: user is admin
            if (loginUser.getUserType() == UserType.ADMIN_USER) {
                return true;
            }
            // case 2: user is project owner
            if (project.getUserId().equals(loginUser.getId())) {
                return true;
            }
            // case 3: check user permission level
            ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), loginUser.getId());
            if (projectUser == null || projectUser.getPerm() != Constants.DEFAULT_ADMIN_PERMISSION) {
                putMsg(result, Status.USER_NO_WRITE_PROJECT_PERM, loginUser.getUserName(), project.getCode());
                checkResult = false;
            } else {
                checkResult = true;
            }
        }
        return checkResult;
    }

    @Override
    public boolean hasProjectAndWritePerm(User loginUser, Project project, Map<String, Object> result) {
        boolean checkResult = false;
        if (project == null) {
            log.error("Project does not exist.");
            putMsg(result, Status.PROJECT_NOT_FOUND, "");
        } else {
            // case 1: user is admin
            if (loginUser.getUserType() == UserType.ADMIN_USER) {
                return true;
            }
            // case 2: user is project owner
            if (project.getUserId().equals(loginUser.getId())) {
                return true;
            }
            // case 3: check user permission level
            ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), loginUser.getId());
            if (projectUser == null || projectUser.getPerm() != Constants.DEFAULT_ADMIN_PERMISSION) {
                putMsg(result, Status.USER_NO_WRITE_PROJECT_PERM, loginUser.getUserName(), project.getCode());
                checkResult = false;
            } else {
                checkResult = true;
            }
        }
        return checkResult;
    }

    @Override
    public void checkHasProjectWritePermissionThrowException(User loginUser, long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        checkHasProjectWritePermissionThrowException(loginUser, project);
    }

    @Override
    public void checkHasProjectWritePermissionThrowException(User loginUser, Project project) {
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, null);
        }
        // case 1: user is admin
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            return;
        }
        // case 2: user is project owner
        if (project.getUserId().equals(loginUser.getId())) {
            return;
        }
        // case 3: check user permission level
        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), loginUser.getId());
        if (projectUser == null || projectUser.getPerm() != Constants.DEFAULT_ADMIN_PERMISSION) {
            throw new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM, loginUser.getUserName(), project.getCode());
        }
    }

    @Override
    public boolean hasProjectAndPerm(User loginUser, Project project, Result result, String permission) {
        boolean checkResult = false;
        if (project == null) {
            log.error("Project does not exist.");
            putMsg(result, Status.PROJECT_NOT_FOUND, "");
        } else if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS,
                permission)) {
            log.error("User does not have {} permission to operate project, userName:{}, projectCode:{}.",
                    permission, loginUser.getUserName(), project.getCode());
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), project.getName());
        } else {
            checkResult = true;
        }
        return checkResult;
    }

    /**
     * 프로젝트 목록을 페이징하여 조회합니다. 관리자는 모든 프로젝트를 볼 수 있습니다.
     *
     * @param loginUser 로그인 사용자
     * @param searchVal 검색어
     * @param pageSize  페이지 크기
     * @param pageNo    페이지 번호
     * @return 로그인 사용자가 볼 수 있는 권한이 있는 프로젝트 목록
     */
    @Override
    public Result queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal) {
        Result result = new Result();
        PageInfo<Project> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<Project> page = new Page<>(pageNo, pageSize);
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<Project> projectIPage =
                projectMapper.queryProjectListPaging(page, new ArrayList<>(projectIds), searchVal);

        List<Project> projectList = projectIPage.getRecords();
        if (loginUser.getUserType() != UserType.ADMIN_USER) {
            for (Project project : projectList) {
                project.setPerm(Constants.DEFAULT_ADMIN_PERMISSION);
            }
        }
        if (CollectionUtils.isEmpty(projectList)) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<User> userList = userMapper.selectByIds(projectList.stream()
                .map(Project::getUserId).distinct().collect(Collectors.toList()));
        Map<Integer, String> userMap = userList.stream().collect(Collectors.toMap(User::getId, User::getUserName));
        List<Long> projectCodes = projectList.stream().map(Project::getCode).distinct().collect(Collectors.toList());
        Map<Long, Integer> projectWorkflowDefinitionCountMap = workflowDefinitionMapper
                .queryProjectWorkflowDefinitionCountByProjectCodes(projectCodes)
                .stream()
                .collect(Collectors.toMap(ProjectWorkflowDefinitionCount::getProjectCode,
                        ProjectWorkflowDefinitionCount::getCount));
        for (Project project : projectList) {
            project.setUserName(userMap.get(project.getUserId()));
            project.setDefCount(projectWorkflowDefinitionCountMap.getOrDefault(project.getCode(), 0));
        }
        pageInfo.setTotal((int) projectIPage.getTotal());
        pageInfo.setTotalList(projectList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 권한 레벨을 포함한 프로젝트 목록을 페이징하여 조회합니다. 관리자는 모든 프로젝트를 볼 수 있습니다.
     *
     * @param userId    사용자 ID
     * @param loginUser 로그인 사용자
     * @param searchVal 검색어
     * @param pageSize  페이지 크기
     * @param pageNo    페이지 번호
     * @return 로그인 사용자의 권한 레벨이 포함된 프로젝트 목록
     */
    @Override
    public Result queryProjectWithAuthorizedLevelListPaging(Integer userId, User loginUser, Integer pageSize,
                                                            Integer pageNo, String searchVal) {
        Result result = new Result();
        PageInfo<Project> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<Project> page = new Page<>(pageNo, pageSize);
        Set<Integer> allProjectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        Set<Integer> userProjectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, userId, log);
        if (allProjectIds.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<Project> projectIPage =
                projectMapper.queryProjectListPaging(page, new ArrayList<>(allProjectIds), searchVal);

        List<Project> projectList = projectIPage.getRecords();

        for (Project project : projectList) {
            if (userProjectIds.contains(project.getId())) {
                ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), userId);
                if (projectUser == null) {
                    // in this case, the user is the project owner, maybe it's better to set it to ALL_PERMISSION.
                    project.setPerm(Constants.DEFAULT_ADMIN_PERMISSION);
                } else {
                    project.setPerm(projectUser.getPerm());
                }
            } else {
                project.setPerm(0);
            }
        }

        pageInfo.setTotal((int) projectIPage.getTotal());
        pageInfo.setTotalList(projectList);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 코드로 프로젝트를 삭제합니다.
     *
     * @param loginUser   로그인 사용자
     * @param projectCode 프로젝트 코드
     * @return 삭제 결과 코드
     */
    @Override
    public Result deleteProject(User loginUser, Long projectCode) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);

        boolean hasProjectAndWritePerm = hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }

        checkProjectAndAuth(result, loginUser, project, project == null ? 0L : project.getCode(), PROJECT_DELETE);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        assert project != null;

        List<WorkflowDefinition> workflowDefinitionList =
                workflowDefinitionMapper.queryAllDefinitionList(project.getCode());

        if (!workflowDefinitionList.isEmpty()) {
            log.warn("Please delete the workflow definitions in project first! project code:{}.", projectCode);
            putMsg(result, Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL);
            return result;
        }
        // delete the task group
        taskGroupService.deleteTaskGroupByProjectCode(project.getCode());

        int delete = projectMapper.deleteById(project.getId());
        if (delete > 0) {
            log.info("Project is deleted and id is :{}.", project.getId());
            result.setData(Boolean.TRUE);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project delete error, project code:{}, project name:{}.", projectCode, project.getName());
            putMsg(result, Status.DELETE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * 확인 결과를 가져옵니다.
     *
     * @param loginUser 로그인 사용자
     * @param project   프로젝트
     * @param perm      권한
     * @return 확인 결과
     */
    private Map<String, Object> getCheckResult(User loginUser, Project project, String perm) {
        Map<String, Object> checkResult =
                checkProjectAndAuth(loginUser, project, project == null ? 0L : project.getCode(), perm);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }
        return null;
    }

    /**
     * 프로젝트 정보를 업데이트합니다.
     *
     * @param loginUser   로그인 사용자
     * @param projectCode 프로젝트 코드
     * @param projectName 프로젝트 이름
     * @param desc        설명
     * @return 업데이트 결과 코드
     */
    @Override
    public Result update(User loginUser, Long projectCode, String projectName, String desc) {
        Result result = new Result();

        checkDesc(result, desc);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        boolean hasProjectAndWritePerm = hasProjectAndWritePerm(loginUser, project, result);
        if (!hasProjectAndWritePerm) {
            return result;
        }
        Project tempProject = projectMapper.queryByName(projectName);
        if (tempProject != null && tempProject.getCode() != project.getCode()) {
            putMsg(result, Status.PROJECT_ALREADY_EXISTS, projectName);
            return result;
        }
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            log.error("user {} not exists", loginUser.getId());
            putMsg(result, Status.USER_NOT_EXIST, loginUser.getId());
            return result;
        }
        project.setName(projectName);
        project.setDescription(desc);
        project.setUpdateTime(new Date());
        project.setUserId(user.getId());
        int update = projectMapper.updateById(project);
        if (update > 0) {
            log.info("Project is updated and id is :{}", project.getId());
            result.setData(project);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Project update error, projectCode:{}, projectName:{}.", project.getCode(), project.getName());
            putMsg(result, Status.UPDATE_PROJECT_ERROR);
        }
        return result;
    }

    /**
     * 권한 레벨이 포함된 모든 프로젝트를 조회합니다.
     *
     * @param loginUser 로그인 사용자
     * @param userId    조회 대상 사용자 ID
     * @return 프로젝트 목록
     */
    @Override
    public Result queryProjectWithAuthorizedLevel(User loginUser, Integer userId) {
        Result result = new Result();

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        List<Project> projectList = projectMapper.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(),
                new ArrayList<>(projectIds));

        List<Project> unauthorizedProjectsList = new ArrayList<>();
        List<Project> authedProjectList = new ArrayList<>();
        Set<Project> projectSet;
        if (projectList != null && !projectList.isEmpty()) {
            projectSet = new HashSet<>(projectList);
            authedProjectList = projectMapper.queryAuthedProjectListByUserId(userId);
            unauthorizedProjectsList = getUnauthorizedProjects(projectSet, authedProjectList);
        }

        for (int i = 0; i < authedProjectList.size(); i++) {
            authedProjectList.get(i).setPerm(7);
        }

        for (int i = 0; i < unauthorizedProjectsList.size(); i++) {
            unauthorizedProjectsList.get(i).setPerm(0);
        }

        List<Project> joined = new ArrayList<>();
        joined.addAll(authedProjectList);
        joined.addAll(unauthorizedProjectsList);

        result.setData(joined);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 권한이 없는 프로젝트를 조회합니다.
     *
     * @param loginUser 로그인 사용자
     * @param userId    사용자 ID
     * @return 사용자가 볼 수 있는 권한이 없는 프로젝트 목록
     */
    @Override
    public Result queryUnauthorizedProject(User loginUser, Integer userId) {
        Result result = new Result();

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Project> projectList = projectMapper.listAuthorizedProjects(
                loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : loginUser.getId(),
                new ArrayList<>(projectIds));

        List<Project> resultList = new ArrayList<>();
        Set<Project> projectSet;
        if (projectList != null && !projectList.isEmpty()) {
            projectSet = new HashSet<>(projectList);

            List<Project> authedProjectList = projectMapper.queryAuthedProjectListByUserId(userId);

            resultList = getUnauthorizedProjects(projectSet, authedProjectList);
        }
        result.setData(resultList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 권한이 없는 프로젝트 목록을 가져옵니다.
     *
     * @param projectSet        전체 프로젝트 셋
     * @param authedProjectList 권한이 있는 프로젝트 목록
     * @return 권한이 없는 프로젝트 목록
     */
    private List<Project> getUnauthorizedProjects(Set<Project> projectSet, List<Project> authedProjectList) {
        List<Project> resultList;
        Set<Project> authedProjectSet;
        if (authedProjectList != null && !authedProjectList.isEmpty()) {
            authedProjectSet = new HashSet<>(authedProjectList);
            projectSet.removeAll(authedProjectSet);
        }
        resultList = new ArrayList<>(projectSet);
        return resultList;
    }

    /**
     * 권한이 있는 프로젝트를 조회합니다.
     *
     * @param loginUser 로그인 사용자
     * @param userId    사용자 ID
     * @return 사용자가 생성한 항목을 제외하고 사용자가 볼 수 있는 권한이 있는 프로젝트 목록
     */
    @Override
    public Result queryAuthorizedProject(User loginUser, Integer userId) {
        Result result = new Result();

        List<Project> projects = projectMapper.queryAuthedProjectListByUserId(userId);
        result.setData(projects);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 권한이 있는 사용자를 조회합니다.
     *
     * @param loginUser   로그인 사용자
     * @param projectCode 프로젝트 코드
     * @return 지정된 프로젝트에 대한 권한이 있는 사용자 목록
     */
    @Override
    public Result queryAuthorizedUser(User loginUser, Long projectCode) {
        Result result = new Result();

        // 1. check read permission
        Project project = this.projectMapper.queryByCode(projectCode);
        boolean hasProjectAndPerm = this.hasProjectAndPerm(loginUser, project, result, PROJECT);
        if (!hasProjectAndPerm) {
            return result;
        }

        // 2. query authorized user list
        List<User> users = this.userMapper.queryAuthedUserListByProjectId(project.getId());
        result.setData(users);
        this.putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 사용자가 생성한 프로젝트를 조회합니다.
     *
     * @param loginUser 로그인 사용자
     * @return 사용자가 생성한 프로젝트 목록
     */
    @Override
    public Map<String, Object> queryProjectCreatedByUser(User loginUser) {
        Map<String, Object> result = new HashMap<>();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(loginUser.getId());
        result.put(Constants.DATA_LIST, projects);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 사용자가 생성했거나 권한을 가진 프로젝트 목록을 조회합니다.
     *
     * @param loginUser 로그인 사용자
     * @return 프로젝트 목록
     */
    @Override
    public Result queryProjectCreatedAndAuthorizedByUser(User loginUser) {
        Result result = new Result();

        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.setData(Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Project> projects = projectMapper.selectBatchIds(projectIds);

        result.setData(projects);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * 읽기 권한이 있는지 확인합니다.
     *
     * @param user    사용자
     * @param project 프로젝트
     * @return 사용자가 프로젝트를 볼 권한이 있는 경우 true, 그렇지 않으면 false
     */
    private boolean checkReadPermission(User user, Project project) {
        int permissionId = queryPermission(user, project);
        return (permissionId & Constants.READ_PERMISSION) != 0;
    }

    /**
     * 권한 ID를 조회합니다.
     *
     * @param user    사용자
     * @param project 프로젝트
     * @return 권한 값
     */
    private int queryPermission(User user, Project project) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return Constants.READ_PERMISSION;
        }

        if (Objects.equals(project.getUserId(), user.getId())) {
            return Constants.ALL_PERMISSIONS;
        }

        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());

        if (projectUser == null) {
            return 0;
        }

        return projectUser.getPerm();

    }

    /**
     * 모든 프로젝트 목록을 조회합니다.
     *
     * @param user 사용자
     * @return 프로젝트 목록
     */
    @Override
    public Result queryAllProjectList(User user) {
        Result result = new Result();
        List<Project> projects =
                projectMapper.queryAllProject(user.getUserType() == UserType.ADMIN_USER ? 0 : user.getId());

        result.setData(projects);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * 프로젝트 및 권한을 확인합니다.
     *
     * @param result      결과 객체
     * @param loginUser   로그인 사용자
     * @param project     프로젝트
     * @param projectCode 프로젝트 코드
     * @param permission  권한
     */
    @Override
    public void checkProjectAndAuth(Result result, User loginUser, Project project, long projectCode,
                                    String permission) {
        if (project == null) {
            log.error("Project does not exist, project code:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_EXIST);
        } else if (!canOperatorPermissions(loginUser, new Object[]{project.getId()}, AuthorizationType.PROJECTS,
                permission)) {
            // check read permission
            putMsg(result, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), projectCode);
        } else {
            putMsg(result, Status.SUCCESS);
        }
    }

    /**
     * 의존성 노드를 위한 모든 프로젝트를 조회합니다.
     *
     * @return 프로젝트 목록
     */
    @Override
    public Result queryAllProjectListForDependent() {
        Result result = new Result<>();
        List<Project> projects =
                projectMapper.queryAllProjectForDependent();
        result.setData(projects);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<Long> getAuthorizedProjectCodes(User loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser can not be null");
        }
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        return projectMapper.selectBatchIds(projectIds)
                .stream()
                .map(Project::getCode)
                .collect(Collectors.toList());
    }
}
