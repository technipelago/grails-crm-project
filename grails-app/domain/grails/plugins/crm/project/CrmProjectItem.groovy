package grails.plugins.crm.project

import grails.plugins.crm.core.TenantUtils

/**
 * Created by goran on 2016-10-05.
 */
class CrmProjectItem {

    public static final List<String> BIND_WHITELIST = [
            'orderIndex', 'name', 'comment', 'budget', 'actual', 'vat'
    ].asImmutable()

    Integer orderIndex
    String name
    String comment
    Double budget
    Double actual
    Double vat

    static belongsTo = [project: CrmProject]

    static constraints = {
        orderIndex()
        name(maxSize: 255, blank: false)
        comment(maxSize: 255, nullable: true)
        budget(nullable: false, min: -9999999d, max: 9999999d, scale: 2)
        actual(nullable: false, min: -9999999d, max: 9999999d, scale: 2)
        vat(nullable: false, min: 0d, max: 1d, scale: 2)
    }

    static transients = ['budgetVAT', 'actualVAT', 'diff', 'diffVAT', 'empty', 'dao']

    def beforeValidate() {
        if (orderIndex == null) {
            orderIndex = 1
        }
        if(budget == null) {
            budget = 0
        }
        if(actual == null) {
            actual = 0
        }
        if(vat == null) {
            vat = 0
        }
    }

    transient Double getBudgetVAT() {
        def p = budget
        if (!p) {
            return 0
        }
        def v = vat ?: 0
        return p + (p * v)
    }

    transient Double getActualVAT() {
        def p = actual
        if (!p) {
            return 0
        }
        def v = vat ?: 0
        return p + (p * v)
    }

    transient Double getDiff() {
        (budget ?: 0) - (actual ?: 0)
    }

    transient Double getDiffVAT() {
        def d = getDiff()
        if(!d) {
            return 0
        }
        def v = vat ?: 0
        return d + (d * v)
    }

    String toString() {
        "$name"
    }

    transient boolean isEmpty() {
        if(name != null) return false
        if(comment != null) return false

        return true
    }

    transient Map<String, Object> getDao() {
        [orderIndex: orderIndex, name: name, comment: comment,
                budget: budget, actual: actual, vat: vat,
                budgetVAT: getBudgetVAT(), actualVAT: getActualVAT()]
    }
}
