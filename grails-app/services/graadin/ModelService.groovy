package graadin

import grails.transaction.Transactional
import groovy.transform.CompileStatic

@CompileStatic
@Transactional
class ModelService {

    @Transactional(readOnly = true)
    List<Model> listAll() {
        Model.list()
    }
}
