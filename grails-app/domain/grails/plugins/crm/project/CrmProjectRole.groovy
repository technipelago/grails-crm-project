package grails.plugins.crm.project

import grails.plugins.crm.contact.CrmContact

/**
 * Created by goran on 2014-08-14.
 */
class CrmProjectRole {

    CrmContact contact
    CrmProjectRoleType type
    String description

    static belongsTo = [project: CrmProject]

    static constraints = {
        contact(unique: ['type', 'project'])
        type()
        description(maxSize: 2000, nullable: true, widget: 'textarea')
    }

    String toString() {
        contact.toString()
    }

}
