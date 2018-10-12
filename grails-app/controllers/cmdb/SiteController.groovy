package cmdb

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class SiteController {

    static scaffold = Site

    SiteService siteService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond siteService.list(params), model:[siteCount: siteService.count()]
    }

    def show(Long id) {
        respond siteService.get(id)
    }

    def create() {
        respond new Site(params)
    }

    def save(Site site) {
        if (site == null) {
            notFound()
            return
        }

        try {
            siteService.save(site)
        } catch (ValidationException e) {
            respond site.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'site.label', default: 'Site'), site.id])
                redirect site
            }
            '*' { respond site, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond siteService.get(id)
    }

    def update(Site site) {
        if (site == null) {
            notFound()
            return
        }

        try {
            siteService.save(site)
        } catch (ValidationException e) {
            respond site.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'site.label', default: 'Site'), site.id])
                redirect site
            }
            '*'{ respond site, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        siteService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'site.label', default: 'Site'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'site.label', default: 'Site'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
