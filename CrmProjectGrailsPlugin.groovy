class CrmProjectGrailsPlugin {
    def groupId = ""
    def version = "2.4.0-SNAPSHOT"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/project/CrmProjectTestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]
    def title = "GR8 CRM Project Management Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Project management for GR8 CRM applications.
'''
    def documentation = "http://gr8crm.github.io/plugins/crm-project/"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-project/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-project"]

    def features = {
        crmProject {
            description "Project Management"
            link controller: "crmProject", action: "index"
            permissions {
                guest "crmProject:index,list,show,createFavorite,deleteFavorite,clearQuery,autocompleteUsername,autocompleteContact"
                partner "crmProject:index,list,show,createFavorite,deleteFavorite,clearQuery,autocompleteUsername,autocompleteContact"
                user "crmProject:*"
                admin "crmProject,crmProjectStatus,crmProjectRoleType:*"
            }
            statistics { tenant ->
                def total = CrmProject.countByTenantId(tenant)
                def updated = CrmProject.countByTenantIdAndLastUpdatedGreaterThan(tenant, new Date() - 31)
                def usage
                if (total > 0) {
                    def tmp = updated / total
                    if (tmp < 0.1) {
                        usage = 'low'
                    } else if (tmp < 0.3) {
                        usage = 'medium'
                    } else {
                        usage = 'high'
                    }
                } else {
                    usage = 'none'
                }
                return [usage: usage, objects: total]
            }
        }
    }
}
