package grails.plugins.crm.project

import grails.plugins.crm.contact.CrmContact
import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.CrmEmbeddedAddress
import grails.plugins.crm.core.TenantEntity
import grails.plugins.sequence.SequenceEntity

/**
 * Created by goran on 2016-04-20.
 */
@TenantEntity
@AuditEntity
@SequenceEntity
class CrmProject {
    String number
    String name
    String description
    CrmProjectStatus status
    String username
    java.sql.Date date1
    java.sql.Date date2
    java.sql.Date date3
    java.sql.Date date4

    CrmEmbeddedAddress address

    static hasMany = [roles: CrmProjectRole]

    static constraints = {
        number(maxSize: 20, blank: false, unique: 'tenantId')
        name(maxSize: 80, blank: false)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        status()
        username(maxSize: 80, nullable: true)
        date1(nullable: true)
        date2(nullable: true)
        date3(nullable: true)
        date4(nullable: true)
        address(nullable: true)
    }

    static embedded = ['address']

    static mapping = {
        sort 'number': 'asc'
        number index: 'crm_project_number_idx'
        name index: 'crm_project_name_idx'
    }

    static transients = ['customer', 'contact', 'dao']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true
    static auditable = true

    static final List<String> BIND_WHITELIST = [
            'number',
            'name',
            'description',
            'status',
            'username',
            'date1',
            'date2',
            'date3',
            'date4'
    ].asImmutable()


    transient CrmContact getCustomer() {
        roles?.find { it.type?.param == 'customer' }?.contact
    }

    transient CrmContact getContact() {
        roles?.find { it.type?.param == 'contact' }?.contact
    }

    def beforeValidate() {
        if (!number) {
            number = getNextSequenceNumber()
        }
    }

    private Map<String, Object> getSelfProperties(List<String> props) {
        props.inject([:]) { m, i ->
            def v = this."$i"
            if (v != null) {
                m[i] = v
            }
            m
        }
    }

    transient Map<String, Object> getDao() {
        final Map<String, Object> map = getSelfProperties(BIND_WHITELIST - 'status')
        map.tenant = tenantId
        map.status = status.dao
        if (address != null) {
            map.address = address.getDao()
        }
        return map
    }

    String toString() {
        name.toString()
    }
}
