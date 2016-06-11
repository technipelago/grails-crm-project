package grails.plugins.crm.project

import grails.plugins.crm.core.CrmLookupEntity

/**
 * This domain class represents one step in the business process.
 */
class CrmProjectStatus extends CrmLookupEntity {

    public static final String CLOSED_PARAM = 'closed'
    public static final String ARCHIVED_PARAM = 'archived'
    public static final List INACTIVE_STATUSES = [9, 19, 29, 39, 49, 59, 69, 79, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 199, 299, 399, 499, 599, 699, 799, 899, 999]

    static transients = ['active']

    boolean isActive() {
        !INACTIVE_STATUSES.contains(orderIndex)
    }
}
