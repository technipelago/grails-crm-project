package grails.plugins.crm.project

import grails.events.Listener
import grails.plugins.crm.contact.CrmContact
import grails.plugins.crm.core.*
import grails.plugins.selection.Selectable
import org.apache.commons.lang.StringUtils
import org.grails.databinding.SimpleMapDataBindingSource
import org.grails.plugin.platform.events.EventMessage

import java.text.DecimalFormat

/**
 * Project Management service methods.
 */
class CrmProjectService {

    def crmCoreService
    def crmSecurityService
    def crmContactService
    def sequenceGeneratorService
    def crmTagService
    def messageSource
    def grailsWebDataBinder

    private String getStatusName(String key, String label, Locale locale) {
        messageSource.getMessage("crmProjectStatus.name." + key, null, label, locale)
    }

    @Listener(namespace = "crmProject", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = crmSecurityService.getTenantInfo(event.tenant)
        def locale = tenant.locale
        TenantUtils.withTenant(tenant.id) {

            crmTagService.createTag(name: CrmProject.name, multiple: true)

            sequenceGeneratorService.initSequence(CrmProject, null, tenant.id, null, "%s")

            // Create default project statuses.
            createProjectStatus([orderIndex: 10, param: "1", name: getStatusName('1', 'Planning', locale)], true)
            createProjectStatus([orderIndex: 30, param: "3", name: getStatusName('3', 'Blocked', locale)], true)
            createProjectStatus([orderIndex: 50, param: "5", name: getStatusName('5', 'Active', locale)], true)
            createProjectStatus([orderIndex: 70, param: "7", name: getStatusName('7', 'Finished', locale)], true)
            createProjectStatus([orderIndex: 90, param: "9", name: getStatusName('9', 'Archived', locale)], true)

            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.customer", null, "Customer", locale), param: "customer", true)
            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.contact", null, "Contact", locale), param: "contact", true)
            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.supplier", null, "Supplier", locale), param: "supplier", true)
            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.manager", null, "Manager", locale), param: "manager", true)
            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.engineer", null, "Engineer", locale), param: "engineer", true)
            createProjectRoleType(name: messageSource.getMessage("crmProjectRoleType.name.consultant", null, "Consultant", locale), param: "consultant", true)
        }
    }

    @Listener(namespace = '*', topic = 'deleted')
    def somethingWasDeleted(EventMessage  event) {
        Map data = (Map)event.data
        crmSecurityService.runAs(data.user, data.tenant) {
            def domain = event.namespace
            def ref = "$domain@${data.id}".toString()
            def projects = CrmProject.createCriteria().list() {
                eq('tenantId', data.tenant)
                eq('ref', ref)
            }
            log.info "Disconnecting ${projects.size()} projects from $ref"
            for(project in projects) {
                try {
                    project.ref = null
                    project.save()
                    log.debug "Project [$project] disconnected from $ref"
                } catch(Exception e) {
                    log.error("Could not disconnect project [$project] from $ref", e)
                }
            }
        }
    }

    // CrmProjectStatus
    //
    CrmProjectStatus getProjectStatus(Long id) {
        def m = CrmProjectStatus.get(id)
        m?.tenantId == TenantUtils.tenant ? m : null
    }

    CrmProjectStatus getProjectStatus(String param) {
        CrmProjectStatus.findByParamAndTenantId(param, TenantUtils.tenant, [cache: true])
    }

    CrmProjectStatus createProjectStatus(params, boolean save = false) {
        if (!params.param) {
            params.param = StringUtils.abbreviate(params.name?.toLowerCase(), 20)
        }
        def tenant = TenantUtils.tenant
        def m = CrmProjectStatus.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmProjectStatus()
            grailsWebDataBinder.bind(m, params as SimpleMapDataBindingSource, null, CrmProjectStatus.BIND_WHITELIST, null, null)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    List<CrmProjectStatus> listProjectStatus(String name, Map params = [:]) {
        CrmProjectStatus.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (name) {
                or {
                    ilike('name', SearchUtils.wildcard(name))
                    eq('param', name)
                }
            }
        }
    }

    // CrmProjectType
    //
    CrmProjectType getProjectType(Long id) {
        def m = CrmProjectType.get(id)
        m?.tenantId == TenantUtils.tenant ? m : null
    }

    CrmProjectType getProjectType(String param) {
        CrmProjectType.findByParamAndTenantId(param, TenantUtils.tenant, [cache: true])
    }

    CrmProjectType createProjectType(params, boolean save = false) {
        if (!params.param) {
            params.param = StringUtils.abbreviate(params.name?.toLowerCase(), 20)
        }
        def tenant = TenantUtils.tenant
        def m = CrmProjectType.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmProjectType()
            grailsWebDataBinder.bind(m, params as SimpleMapDataBindingSource, null, CrmProjectType.BIND_WHITELIST, null, null)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    List<CrmProjectType> listProjectType(String name, Map params = [:]) {
        CrmProjectType.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (name) {
                or {
                    ilike('name', SearchUtils.wildcard(name))
                    eq('param', name)
                }
            }
        }
    }

    // CrmProjectCategory
    //
    CrmProjectCategory getProjectCategory(Long id) {
        def m = CrmProjectCategory.get(id)
        m?.tenantId == TenantUtils.tenant ? m : null
    }

    CrmProjectCategory getProjectCategory(String param) {
        CrmProjectCategory.findByParamAndTenantId(param, TenantUtils.tenant, [cache: true])
    }

    CrmProjectCategory createProjectCategory(params, boolean save = false) {
        if (!params.param) {
            params.param = StringUtils.abbreviate(params.name?.toLowerCase(), 20)
        }
        def tenant = TenantUtils.tenant
        def m = CrmProjectCategory.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmProjectCategory()
            grailsWebDataBinder.bind(m, params as SimpleMapDataBindingSource, null, CrmProjectCategory.BIND_WHITELIST, null, null)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    List<CrmProjectCategory> listProjectCategory(String name, Map params = [:]) {
        CrmProjectCategory.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (name) {
                or {
                    ilike('name', SearchUtils.wildcard(name))
                    eq('param', name)
                }
            }
        }
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List or CrmProject domain instances
     */
    @Selectable
    PagedResultList<CrmProject> list(Map params = [:]) {
        list([:], params)
    }

    /**
     * Find CrmProject instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List or CrmProject domain instances
     */
    @Selectable
    synchronized PagedResultList<CrmProject> list(Map query, Map params) {
        def result
        try {
            Set<Long> reduce = [] as Set
            if (query.tags) {
                def tagged = crmTagService.findAllIdByTag(CrmProject, query.tags)
                if (tagged) {
                    reduce.addAll(tagged)
                } else {
                    reduce = NO_RESULT
                }
            }

            if (query.ref) {
                def refIds = referenceQuery('ref', query.ref)
                if (refIds) {
                    if(reduce) {
                        reduce.retainAll(refIds)
                    } else {
                        reduce.addAll(refIds)
                    }
                    if (reduce.isEmpty()) {
                        reduce = NO_RESULT
                    }
                } else {
                    reduce = NO_RESULT
                }
            }

            if (reduce == NO_RESULT) {
                // No need to execute main query, result is already given (nothing, nada, empty, zero).
                return new PagedResultList<CrmProject>([])
            }

            def totalCount = CrmProject.createCriteria().get {
                projectCriteria.delegate = delegate
                projectCriteria(query, reduce, true, null)
            }
            if (!params.sort) params.sort = 'number'
            if (!params.order) params.order = 'asc'
            def ids = CrmProject.createCriteria().list() {
                projectCriteria.delegate = delegate
                projectCriteria(query, reduce, false, params.sort)
                if (params.max != null) {
                    maxResults(params.max as Integer)
                }
                if (params.offset != null) {
                    firstResult(params.offset as Integer)
                }
                order(params.sort, params.order)
            }
            result = ids.size() ? ids.collect { CrmProject.get(it[0]) } : []
            result = new PagedResultList<CrmProject>(result, totalCount.intValue())
        } finally {
            projectCriteria.delegate = this
        }
        return result
    }

    private Collection<Long> referenceQuery(String propertyName, String query) {
        def tenant = TenantUtils.tenant
        def refs = event(for: "crmProject", topic: "query",
                data: [tenant: tenant, property: propertyName, query: query],
                fork: false).values.flatten()
        def result
        if (refs) {
            result = CrmProject.createCriteria().list() {
                projections {
                    property('id')
                }
                eq('tenantId', tenant)
                inList('ref', refs)
            }
        }
        result
    }

    private static final Set<Long> NO_RESULT = [0L] as Set // A query value that will find nothing

    private Closure projectCriteria = { query, ids, count, sort ->
        def locale = query.locale ?: new Locale("sv", "SE")
        def timezone = query.timezone ?: TimeZone.getTimeZone("MET")

        projections {
            if (count) {
                countDistinct "id"
            } else {
                groupProperty "id"
                groupProperty sort
            }
        }

        eq('tenantId', TenantUtils.tenant)
        if (ids) {
            inList('id', ids)
        }

        if (query.number) {
            ilike('number', SearchUtils.wildcard(query.number))
        }
        if (query.name) {
            ilike('name', SearchUtils.wildcard(query.name))
        }

        if (query.parent) {
            parent {
                or {
                    eq('number', query.parent)
                    ilike('name', SearchUtils.wildcard(query.parent))
                }
            }
        }

        if(query.reference) {
            eq('ref', crmCoreService.getReferenceIdentifier(query.reference))
        }

        if (query.username) {
            ilike('username', SearchUtils.wildcard(query.username))
        }

        if (query.status) {
            if(query.status instanceof CrmProjectStatus) {
                eq('status', query.status)
            } else {
                status {
                    or {
                        eq('param', query.status)
                        ilike('name', SearchUtils.wildcard(query.status))
                    }
                }
            }
        }

        if (query.type) {
            if(query.type instanceof CrmProjectType) {
                eq('type', query.type)
            } else {
                type {
                    or {
                        eq('param', query.type)
                        ilike('name', SearchUtils.wildcard(query.type))
                    }
                }
            }
        }

        if (query.category) {
            if(query.category instanceof CrmProjectCategory) {
                eq('category', query.category)
            } else {
                category {
                    or {
                        eq('param', query.category)
                        ilike('name', SearchUtils.wildcard(query.category))
                    }
                }
            }
        }

        // TODO this will find any project role, not just customer.
        def customer = query.customer
        if (customer) {
            roles {
                if (customer instanceof CrmContact) {
                    eq('contact', customer)
                } else if (customer instanceof Number) {
                    contact {
                        eq('id', customer)
                    }
                } else {
                    contact {
                        ilike('name', SearchUtils.wildcard(customer.toString()))
                    }
                }
            }
        }

        if (query.address1) {
            ilike('address.address1', SearchUtils.wildcard(query.address1))
        }
        if (query.address2) {
            ilike('address.address2', SearchUtils.wildcard(query.address2))
        }
        if (query.address3) {
            ilike('address.address3', SearchUtils.wildcard(query.address3))
        }
        if (query.postalCode) {
            ilike('address.postalCode', SearchUtils.wildcard(query.postalCode))
        }
        if (query.city) {
            ilike('address.city', SearchUtils.wildcard(query.city))
        }
        if (query.region) {
            ilike('address.region', SearchUtils.wildcard(query.region))
        }
        if (query.country) {
            ilike('address.country', SearchUtils.wildcard(query.country))
        }

        if (query.value) {
            doubleQuery(delegate, 'value', query.value, locale)
        }
        if (query.date1) {
            sqlDateQuery(delegate, 'date1', query.date1, locale, timezone)
        }
        if (query.date2) {
            sqlDateQuery(delegate, 'date2', query.date2, locale, timezone)
        }
        if (query.date3) {
            sqlDateQuery(delegate, 'date3', query.date3, locale, timezone)
        }
        if (query.date4) {
            sqlDateQuery(delegate, 'date4', query.date4, locale, timezone)
        }

        if (query.dateCreated) {
            if (query.dateCreated[0] == '<') {
                lt('dateCreated', DateUtils.getDateSpan(DateUtils.parseDate(query.dateCreated.substring(1)))[0])
            } else if (query.dateCreated[0] == '>') {
                gt('dateCreated', DateUtils.getDateSpan(DateUtils.parseDate(query.dateCreated.substring(1)))[1])
            } else {
                def (am, pm) = DateUtils.getDateSpan(DateUtils.parseDate(query.dateCreated))
                between('dateCreated', am, pm)
            }
        }

        if (query.lastUpdated) {
            if (query.lastUpdated[0] == '<') {
                lt('lastUpdated', DateUtils.getDateSpan(DateUtils.parseDate(query.lastUpdated.substring(1)))[0])
            } else if (query.lastUpdated[0] == '>') {
                gt('lastUpdated', DateUtils.getDateSpan(DateUtils.parseDate(query.lastUpdated.substring(1)))[1])
            } else {
                def (am, pm) = DateUtils.getDateSpan(DateUtils.parseDate(query.lastUpdated))
                between('lastUpdated', am, pm)
            }
        }
    }

    /**
     * Parse a query string and apply criteria for a Double property.
     *
     * @param criteriaDelegate
     * @param prop the property to query
     * @param query the query string, can contain &lt; &gt and -
     * @param locale the locale to use for number parsing
     */
    private void doubleQuery(Object criteriaDelegate, String prop, String query, Locale locale) {
        if (!query) {
            return
        }
        final DecimalFormat format = DecimalFormat.getNumberInstance(locale)
        if (query[0] == '<') {
            criteriaDelegate.lt(prop, format.parse(query.substring(1)).doubleValue())
        } else if (query[0] == '>') {
            criteriaDelegate.gt(prop, format.parse(query.substring(1)).doubleValue())
        } else if (query.contains('-')) {
            def (from, to) = query.split('-').toList()
            criteriaDelegate.between(prop, format.parse(from).doubleValue(), format.parse(to).doubleValue())
        } else if (query.contains(' ')) {
            def (from, to) = query.split(' ').toList()
            criteriaDelegate.between(prop, format.parse(from).doubleValue(), format.parse(to).doubleValue())
        } else {
            criteriaDelegate.eq(prop, format.parse(query).doubleValue())
        }
    }

    /**
     * Parse a query string and apply criteria for a java.sql.Date property.
     *
     * @param criteriaDelegate
     * @param prop the property to query
     * @param query the query string, can contain &lt &gt and -
     * @param locale the locale to use for date parsing
     * @param timezone the timezone to use for date parsing
     */
    private void sqlDateQuery(Object criteriaDelegate, String prop, String query, Locale locale, TimeZone timezone) {
        if (!query) {
            return
        }
        if (query[0] == '<') {
            criteriaDelegate.lt(prop, DateUtils.parseSqlDate(query.substring(1), timezone))
        } else if (query[0] == '>') {
            criteriaDelegate.gt(prop, DateUtils.parseSqlDate(query.substring(1), timezone))
        } else if (query.contains(' ')) {
            def (from, to) = query.split(' ').toList()
            criteriaDelegate.between(prop, DateUtils.parseSqlDate(from, timezone), DateUtils.parseSqlDate(to, timezone))
        } else {
            criteriaDelegate.eq(prop, DateUtils.parseSqlDate(query, timezone))
        }
    }


    CrmProject getProject(Long id) {
        CrmProject.findByIdAndTenantId(id, TenantUtils.tenant)
    }

    CrmProject createProject(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def currentUser = crmSecurityService.currentUser
        def customer = createProjectRoleType(name: "Customer", true)
        def contact = createProjectRoleType(name: "Contact", true)
        def m = new CrmProject()
        grailsWebDataBinder.bind(m, params as SimpleMapDataBindingSource, null, CrmProject.BIND_WHITELIST, null, null)
        m.tenantId = tenant
        if (!m.username) {
            m.username = currentUser?.username
        }
        if (!m.status) {
            m.status = CrmProject.withNewSession {
                CrmProjectStatus.createCriteria().get() {
                    eq('tenantId', tenant)
                    order 'orderIndex', 'asc'
                    maxResults 1
                }
            }
        }
        if (!m.date1) {
            m.date1 = new java.sql.Date(System.currentTimeMillis())
        }
        if (params.reference) {
            m.setReference(params.reference)
        }
        if (params.customer) {
            def role = new CrmProjectRole(project: m, contact: params.customer, type: customer)
            if (!role.hasErrors()) {
                m.addToRoles(role)
            }

        }
        if (params.contact) {
            def role = new CrmProjectRole(project: m, contact: params.contact, type: contact)
            if (!role.hasErrors()) {
                m.addToRoles(role)
            }
        }
        if (save) {
            m.save(failOnError: true, flush: true)
            event(for: "crmProject", topic: "created", fork: false,
                    data: [id: m.id, tenant: m.tenantId, crmProject: m, user: m.username])
        } else {
            m.validate()
            m.clearErrors()
        }
        return m
    }

    String deleteProject(CrmProject crmProject) {
        def tombstone = crmProject.toString()
        def id = crmProject.id
        def tenant = crmProject.tenantId
        def username = crmSecurityService.currentUser?.username
        event(for: "crmProject", topic: "delete", fork: false, data: [id: id, tenant: tenant, user: username, name: tombstone])
        crmProject.delete(flush: true)
        log.debug "Deleted project #$id in tenant $tenant \"${tombstone}\""
        event(for: "crmProject", topic: "deleted", data: [id: id, tenant: tenant, user: username, name: tombstone])
        return tombstone
    }

    CrmProjectRole addRole(CrmProject project, CrmContact contact, Object role, String description = null) {
        def type
        if (role instanceof CrmProjectRoleType) {
            type = role
        } else {
            type = getProjectRoleType(role.toString())
            if (!type) {
                throw new IllegalArgumentException("[$role] is not a valid project role")
            }
        }
        def roleInstance = new CrmProjectRole(project: project, contact: contact, type: type, description: description)
        if (!roleInstance.hasErrors()) {
            project.addToRoles(roleInstance)
        }
        return roleInstance
    }

    boolean deleteRole(CrmProject project, CrmContact contact, Object role = null) {
        def type
        if (role) {
            if (role instanceof CrmProjectRoleType) {
                type = role
            } else {
                type = getProjectRoleType(role.toString())
            }
        }
        def roleInstance = project.roles?.find { CrmProjectRole r ->
            r.contactId == contact.id && (type == null || type == r.type)
        }
        if (roleInstance) {
            project.removeFromRoles(roleInstance)
            roleInstance.delete()
            return true
        }
        return false
    }

    CrmProjectRoleType getProjectRoleType(String param) {
        CrmProjectRoleType.findByParamAndTenantId(param, TenantUtils.tenant, [cache: true])
    }

    CrmProjectRoleType createProjectRoleType(params, boolean save = false) {
        if (!params.param) {
            params.param = StringUtils.abbreviate(params.name?.toLowerCase(), 20)
        }
        def tenant = TenantUtils.tenant
        def m = CrmProjectRoleType.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmProjectRoleType()
            grailsWebDataBinder.bind(m, params as SimpleMapDataBindingSource, null, CrmProjectRoleType.BIND_WHITELIST, null, null)
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    List<CrmProjectRoleType> listProjectRoleType(String name, Map params = [:]) {
        CrmProjectRoleType.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (name) {
                or {
                    ilike('name', SearchUtils.wildcard(name))
                    eq('param', name)
                }
            }
        }
    }

    List<CrmProject> findProjectsByReference(Object reference, Map params = [:]) {
        CrmProject.findAllByRef(crmCoreService.getReferenceIdentifier(reference), params)
    }

    List<CrmProject> findProjectsByContact(CrmContact contact, String role = null, Map params = [:]) {
        CrmProject.createCriteria().list(params) {
            eq('tenantId', contact.tenantId) // This is not necessary, but hopefully it helps the query optimizer
            roles {
                eq('contact', contact)
                if (role) {
                    type {
                        or {
                            ilike('name', SearchUtils.wildcard(role))
                            eq('param', role)
                        }
                    }
                }
            }
        }
    }

    List<CrmProjectRole> findProjectRolesByContact(CrmContact contact, String role = null, Map params = [:]) {
        CrmProjectRole.createCriteria().list(params) {
            project {
                eq('tenantId', contact.tenantId) // This is not necessary, but maybe it helps the query optimizer
            }
            eq('contact', contact)
            if (role) {
                type {
                    or {
                        ilike('name', SearchUtils.wildcard(role))
                        eq('param', role)
                    }
                }
            }
        }
    }

    CrmProject save(CrmProject crmProject, Map params) {
        def tenant = TenantUtils.tenant
        if (crmProject.tenantId) {
            if (crmProject.tenantId != tenant) {
                throw new IllegalStateException("The current tenant is [$tenant] and the specified domain instance belongs to another tenant [${crmProject.tenantId}]")
            }
        } else {
            crmProject.tenantId = tenant
        }
        def customerType = createProjectRoleType(name: "Customer", true)
        def contactType = createProjectRoleType(name: "Contact", true)
        def (company, contact) = fixCustomerParams(params)
        def currentUser = crmSecurityService.getUserInfo()

        try {
            bindDate(crmProject, 'date1', params.date1, currentUser?.timezone)
            bindDate(crmProject, 'date2', params.date2, currentUser?.timezone)
            bindDate(crmProject, 'date3', params.date3, currentUser?.timezone)
            bindDate(crmProject, 'date4', params.date4, currentUser?.timezone)
        } catch (CrmValidationException e) {
            throw new CrmValidationException(e.message, crmProject, company, contact)
        }

        grailsWebDataBinder.bind(crmProject, params as SimpleMapDataBindingSource, null,
                CrmProject.BIND_WHITELIST, ['date1', 'date2', 'date3', 'date4'], null)

        if (!crmProject.status) {
            crmProject.status = CrmProject.withNewSession {
                CrmProjectStatus.createCriteria().get() {
                    eq('tenantId', tenant)
                    order 'orderIndex', 'asc'
                    maxResults 1
                }
            }
        }
        if (!crmProject.date1) {
            crmProject.date1 = new java.sql.Date(System.currentTimeMillis())
        }

        def existingCompany = crmProject.roles?.find { it.type == customerType }
        if (company) {
            if (existingCompany) {
                existingCompany.contact = company
            } else {
                def role = new CrmProjectRole(project: crmProject, contact: company, type: customerType)
                if (!role.hasErrors()) {
                    crmProject.addToRoles(role)
                }
            }
        }

        def existingContact = crmProject.roles?.find { it.type == contactType }
        if (contact) {
            if (existingContact) {
                existingContact.contact = contact
            } else {
                def role = new CrmProjectRole(project: crmProject, contact: contact, type: contactType)
                if (!role.hasErrors()) {
                    crmProject.addToRoles(role)
                }
            }
        }

        if (params.reference || params.ref) {
            crmProject.setReference(params.reference ?: params.ref)
        }

        // Bind items.
        if (params.items instanceof List) {
            for (row in params.items) {
                def item = row.id ? CrmProjectItem.get(row.id) : new CrmProjectItem(project: crmProject)
                grailsWebDataBinder.bind(item, row as SimpleMapDataBindingSource, null, CrmProjectItem.BIND_WHITELIST, null, null)
                if (item.id) {
                    if (item.isEmpty()) {
                        crmProject.removeFromItems(item)
                        item.delete()
                    } else {
                        item.save()
                    }
                } else if (!item.hasErrors() && !item.isEmpty()) {
                    crmProject.addToItems(item)
                }
            }
        } else {
            bindItems(crmProject, params)
        }

        if (crmProject.save()) {
            return crmProject
        }

        throw new CrmValidationException('crmProject.validation.error', crmProject, company, contact)
    }

    private void bindDate(def target, String property, String value, TimeZone timezone = null) {
        if (value) {
            def tenant = crmSecurityService.getCurrentTenant()
            def locale = tenant?.localeInstance ?: Locale.getDefault()
            try {
                target[property] = DateUtils.parseSqlDate(value, timezone)
            } catch (Exception e) {
                def entityName = messageSource.getMessage('crmProject.label', null, 'Project', locale)
                def propertyName = messageSource.getMessage('crmProject.' + property + '.label', null, property, locale)
                target.errors.rejectValue(property, 'default.invalid.date.message', [propertyName, entityName, value.toString(), e.message].toArray(), "Invalid date: {2}")
                throw new CrmValidationException('crmProject.invalid.date.message', target)
            }
        } else {
            target[property] = null
        }
    }

    private void bindItems(CrmProject crmProject, Map params) {
        // This is a workaround for Grails 2.4.4 data binding that does not insert a new CrmProjectItem when 'id' is null.
        // I consider this to be a bug in Grails 2.4.4 but I'm not sure how it's supposed to work with Set.
        // This workaround was not needed in Grails 2.2.4.
        int i = 0
        int miss = 0
        while (miss < 10) {
            def a = params["items[$i]".toString()]
            if (a?.id) {
                def item = CrmProjectItem.get(a.id)
                if (crmProject.id != item?.projectId) {
                    throw new RuntimeException("CrmProjectItem [${item.projectId}] is not associated with CrmProject [${crmProject.id}]")
                }
                grailsWebDataBinder.bind(item, a as SimpleMapDataBindingSource, null, CrmProjectItem.BIND_WHITELIST, null, null)
                if (item.isEmpty()) {
                    crmProject.removeFromItems(item)
                    item.delete()
                } else {
                    item.save()
                }
            } else if (a) {
                def item = new CrmProjectItem(project: crmProject)
                grailsWebDataBinder.bind(item, a as SimpleMapDataBindingSource, null, CrmProjectItem.BIND_WHITELIST, null, null)
                if (!item.isEmpty()) {
                    if (item.validate()) {
                        crmProject.addToItems(item)
                    } else {
                        for (error in item.errors.allErrors) {
                            crmProject.errors.reject(error.getDefaultMessage())
                        }
                        //crmProject.errors.addAllErrors(item.errors)
                    }
                }
            } else {
                miss++
            }
            i++
        }
    }

    private List fixCustomerParams(Map params) {
        def company
        def contact
        if (params.customer instanceof CrmContact) {
            company = params.customer
        } else if (params['customer.id']) {
            company = crmContactService.getContact(Long.valueOf(params['customer.id'].toString()))
        }
        if (params.contact instanceof CrmContact) {
            contact = params.contact
        } else if (params['contact.id']) {
            contact = crmContactService.getContact(Long.valueOf(params['contact.id'].toString()))
        }

        if (company == null) {
            def primaryContact = contact?.primaryContact
            if (primaryContact) {
                // Company is not specified but the selected person is associated with a company (primaryContact)
                // Set params as if the user had selected the person's primary contact in the company field.
                company = primaryContact
                params['customer.name'] = company.name
                params['customer.id'] = company.id
            }
        }

        // A company name is specified but it's not an existing company.
        // Create a new company.
        if (params['customer.name'] && !company) {
            company = crmContactService.createCompany(name: params['customer.name']).save(failOnError: true, flush: true)
            params['customer.id'] = company.id
        }

        // A person name is specified but it's not an existing person.
        // Create a new person.
        if (params['contact.name'] && !contact) {
            contact = crmContactService.createPerson([firstName: params['contact.name'], related: company]).save(failOnError: true, flush: true)
            params['contact.id'] = contact.id
        }

        return [company, contact]
    }

}
