package graadin

import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewDisplay
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.annotation.SpringViewDisplay
import com.vaadin.ui.*
import groovy.transform.CompileStatic

@CompileStatic
@SpringUI(path="/vaadinui")
@Title("Grails Vaadin")
@Theme("graadin-theme")
@SpringViewDisplay
class GraadinGrailsUI extends UI implements ViewDisplay {
    private Panel springViewDisplay

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout root = new VerticalLayout()

        root.setSizeFull()
        setContent(root)

        springViewDisplay = new Panel()
        springViewDisplay.setSizeFull()

        root.addComponent(buildHeader())
        root.addComponent(springViewDisplay)
        root.setExpandRatio(springViewDisplay, 1.0f)
    }

    static private Label buildHeader() {
        final Label mainTitle = new Label("Welcome to the Garage")

        mainTitle
    }

    @Override
    void showView(final View view) {
        springViewDisplay.setContent((Component) view)
    }
}
