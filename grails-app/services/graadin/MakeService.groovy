package graadin

import grails.transaction.Transactional
import groovy.transform.CompileStatic

@CompileStatic
@Transactional
class MakeService {

    @Transactional(readOnly = true)
    List<Make> listAll() {
        Make.list()
    }
}
