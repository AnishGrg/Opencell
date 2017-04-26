package org.meveo.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.SecuredEntityDto;
import org.meveo.api.dto.UserDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.admin.SecuredEntity;
import org.meveo.model.admin.User;
import org.meveo.model.hierarchy.UserHierarchyLevel;
import org.meveo.model.security.Role;
import org.meveo.model.shared.Name;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.hierarchy.impl.UserHierarchyLevelService;
import org.meveo.service.security.SecuredBusinessEntityService;

/**
 * @author Edward P. Legaspi
 **/
@Stateless
public class UserApi extends BaseApi {

	@Inject
	private RoleService roleService;

	@Inject
	private UserService userService;

    @Inject
	private SecuredBusinessEntityService securedBusinessEntityService;
    
	@Inject
    private UserHierarchyLevelService userHierarchyLevelService;

	public void create(UserDto postData) throws MeveoApiException, BusinessException {

		if (StringUtils.isBlank(postData.getUsername())) {
			missingParameters.add("username");
		}
		if (StringUtils.isBlank(postData.getEmail())) {
			missingParameters.add("email");
		}

		if ((postData.getRoles() == null || postData.getRoles().isEmpty()) && StringUtils.isBlank(postData.getRole())) {
			missingParameters.add("roles");
		}

		handleMissingParameters();

		if (!StringUtils.isBlank(postData.getRole())) {
			if (postData.getRoles() == null) {
				postData.setRoles(new ArrayList<String>());
			}
			postData.getRoles().add(postData.getRole());
		}

		if (!(currentUser.hasRole("userManagement") || currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
			throw new ActionForbiddenException("User has no permission to manage users for provider");
		}

		// check if the user already exists
		if (userService.findByUsername(postData.getUsername()) != null) {
			throw new EntityAlreadyExistsException(User.class, postData.getUsername(), "username");
		}

		// find role
		Set<Role> roles = extractRoles(postData);

		// parse secured entities
		List<SecuredEntity> securedEntities = extractSecuredEntities(postData);

        UserHierarchyLevel userHierarchyLevel = null;
        if(!StringUtils.isBlank(postData.getUserLevel())){
            userHierarchyLevel = userHierarchyLevelService.findByCode(postData.getUserLevel());
            if (userHierarchyLevel == null) {
                throw new EntityDoesNotExistsException(UserHierarchyLevel.class, "userLevel");
            }
        }

		User user = new User();
		user.setUserName(postData.getUsername().toUpperCase());
		user.setEmail((postData.getEmail()));
		Name name = new Name();
		name.setLastName(postData.getLastName());
		name.setFirstName(postData.getFirstName());
		user.setName(name);
		user.setPassword(postData.getPassword());
		user.setLastPasswordModification(new Date());
		user.setRoles(roles);
		user.setSecuredEntities(securedEntities);
        user.setUserLevel(userHierarchyLevel);

		userService.create(user);
	}

	public void update(UserDto postData) throws MeveoApiException, BusinessException {
		if (StringUtils.isBlank(postData.getUsername())) {
			missingParameters.add("username");
		}
		handleMissingParameters();
		
		//we support old dto that containt only one role
		if (!StringUtils.isBlank(postData.getRole())) {
			if (postData.getRoles() == null) {
				postData.setRoles(new ArrayList<String>());
			}
			postData.getRoles().add(postData.getRole());
		}

		// find user
		User user = userService.findByUsername(postData.getUsername());

		if (user == null) {
			throw new EntityDoesNotExistsException(User.class, postData.getUsername(), "username");
		}

        if (!(currentUser.hasRole("userManagement") || currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization")))) {
			throw new ActionForbiddenException("User has no permission to manage users for provider");
		}

		// find roles
		Set<Role> roles = extractRoles(postData);

		// parse secured entities
		List<SecuredEntity> securedEntities = extractSecuredEntities(postData);

        UserHierarchyLevel userHierarchyLevel = null;
        if(!StringUtils.isBlank(postData.getUserLevel())){
            userHierarchyLevel = userHierarchyLevelService.findByCode(postData.getUserLevel());
            if (userHierarchyLevel == null) {
                throw new EntityDoesNotExistsException(UserHierarchyLevel.class, "userLevel");
            }
        }

		user.setUserName(postData.getUsername());
		if (!StringUtils.isBlank(postData.getEmail())) {
			user.setEmail(postData.getEmail());
		}
		if (!StringUtils.isBlank(postData.getPassword())) {
			user.setNewPassword(postData.getPassword());
		}
		Name name = new Name();
		if (!StringUtils.isBlank(postData.getLastName())) {
			name.setLastName(postData.getLastName());
			user.setName(name);
		}
		if (!StringUtils.isBlank(postData.getFirstName())) {
			name.setFirstName(postData.getFirstName());
			user.setName(name);
		}
		if (!roles.isEmpty()) {
			user.setRoles(roles);
		}
		user.setSecuredEntities(securedEntities);
        user.setUserLevel(userHierarchyLevel);

		userService.update(user);
	}

	private Set<Role> extractRoles(UserDto postData) throws EntityDoesNotExistsException {
		Set<Role> roles = new HashSet<Role>();
		if(postData.getRoles() == null){
			return roles;
		}
		for (String rl : postData.getRoles()) {
			Role role = roleService.findByName(rl);
			if (role == null) {
				throw new EntityDoesNotExistsException(Role.class, rl);
			}
			roles.add(role);
		}
		return roles;
	}

	private List<SecuredEntity> extractSecuredEntities(UserDto postData) throws EntityDoesNotExistsException {
		List<SecuredEntity> securedEntities = new ArrayList<>();
		if (postData.getSecuredEntities() != null) {
			SecuredEntity securedEntity = null;
			for (SecuredEntityDto securedEntityDto : postData.getSecuredEntities()) {
				securedEntity = new SecuredEntity();
				securedEntity.setCode(securedEntityDto.getCode());
				securedEntity.setEntityClass(securedEntityDto.getEntityClass());
				BusinessEntity businessEntity = securedBusinessEntityService.getEntityByCode(securedEntity.getEntityClass(), securedEntity.getCode());
				if (businessEntity == null) {
					throw new EntityDoesNotExistsException(securedEntity.getEntityClass(), securedEntity.getCode());
				}
				securedEntities.add(securedEntity);
			}
		}
		return securedEntities;
	}

	public void remove(String username) throws MeveoApiException, BusinessException {
		User user = userService.findByUsername(username);

		if (user == null) {
			throw new EntityDoesNotExistsException(User.class, username, "username");
		}

        if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization")))) {
			throw new ActionForbiddenException("User has no permission to manage users for provider");
		}

		userService.remove(user);
	}

	public UserDto find(String username) throws MeveoApiException {

		if (StringUtils.isBlank(username)) {
			missingParameters.add("username");
		}

		handleMissingParameters();

        User user = userService.findByUsernameWithFetch(username, Arrays.asList("roles", "userLevel"));

		if (user == null) {
			throw new EntityDoesNotExistsException(User.class, username, "username");
		}

        if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization")))) {
			throw new ActionForbiddenException("User has no permission to access users for provider");
		}

		UserDto result = new UserDto(user);

		return result;
	}

	public void createOrUpdate(UserDto postData) throws MeveoApiException, BusinessException {
		User user = userService.findByUsername(postData.getUsername());
		if (user == null) {
			create(postData);
		} else {
			update(postData);
		}
	}
}
