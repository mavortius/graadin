package graadin

import com.vaadin.data.HasValue
import com.vaadin.data.ValueProvider
import com.vaadin.event.selection.SingleSelectionEvent
import com.vaadin.event.selection.SingleSelectionListener
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.Button
import com.vaadin.ui.ComboBox
import com.vaadin.ui.Grid
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.ItemCaptionGenerator
import com.vaadin.ui.Label
import com.vaadin.ui.TextField
import com.vaadin.ui.VerticalLayout
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

@Slf4j
@CompileStatic
@SpringView(name = GarageView.VIEW_NAME)
class GarageView extends VerticalLayout implements View {
    public static final String VIEW_NAME = ""

    @Autowired
    private DriverService driverService

    @Autowired
    private MakeService makeService

    @Autowired
    private ModelService modelService

    @Autowired
    private VehicleService vehicleService

    private Vehicle vehicle = new Vehicle()

    @PostConstruct
    void init() {
        // Display Row One: (Add panel title)
        final HorizontalLayout titleRow = new HorizontalLayout()
        titleRow.setWidth("100%")
        addComponent(titleRow)

        final Label title = new Label("Add a Vehicle")
        titleRow.addComponent(title)
        titleRow.setExpandRatio(title, 1.0f)

        //Display Row Two: (Build data input)
        final HorizontalLayout inputRow = new HorizontalLayout()
        inputRow.setWidth("100%")
        addComponent(inputRow)

        // Build data input constructs
        final TextField vehicleName = buildNewVehicleName()
        final ComboBox<Make> vehicleMake = this.buildMakeComponent()
        final ComboBox<Model> vehicleModel = this.buildModelComponent()
        final ComboBox<Driver> vehicleDriver = this.buildDriverComponent()
        final Button submitBtn = buildSubmitButton()

        // Add listeners to capture data change
        //tag::listeners[]
        vehicleName.addValueChangeListener(new UpdateVehicleValueChangeListener('NAME'))
        vehicleMake.addSelectionListener(new UpdateVehicleComboBoxSelectionListener('MAKE'))
        vehicleModel.addSelectionListener(new UpdateVehicleComboBoxSelectionListener('MODEL'))
        vehicleDriver.addSelectionListener(new UpdateVehicleComboBoxSelectionListener('DRIVER'))
        submitBtn.addClickListener { event ->
            this.submit()
        }
        //end::listeners[]

        // Add data constructs to row
        [vehicleName, vehicleMake, vehicleModel, vehicleDriver, submitBtn].each {
            inputRow.addComponent(it)
        }

        // Display Row Three: (Display all vehicles in database)
        final HorizontalLayout dataDisplayRow = new HorizontalLayout()
        dataDisplayRow.setWidth("100%")
        addComponent(dataDisplayRow)
        dataDisplayRow.addComponent(this.buildVehicleComponent())
    }

    class UpdateVehicleValueChangeListener implements HasValue.ValueChangeListener {
        String eventType

        UpdateVehicleValueChangeListener(String eventType) {
            this.eventType = eventType
        }

        @Override
        void valueChange(HasValue.ValueChangeEvent event) {
            updateVehicle(eventType, event.value)
        }
    }

    class UpdateVehicleComboBoxSelectionListener implements SingleSelectionListener {
        String eventType

        UpdateVehicleComboBoxSelectionListener(String eventType) {
            this.eventType = eventType
        }

        @Override
        void selectionChange(SingleSelectionEvent event) {
            updateVehicle(eventType, event.firstSelectedItem)
        }
    }

    @Override
    void enter(ViewChangeListener.ViewChangeEvent event) {
        // This view is constructed in the init() method()
    }

    // Private UI component builders
    static private TextField buildNewVehicleName() {
        final TextField vehicleName = new TextField()

        vehicleName.setPlaceholder("Enter a name...")

        vehicleName
    }

    private ComboBox<Make> buildMakeComponent() {
        final List<Make> makes = makeService.listAll()
        final ComboBox<Make> select = new ComboBox<>()

        select.with {
            emptySelectionAllowed = false
            placeholder = "Select a Make"
            itemCaptionGenerator = new CustomItemCaptionGenerator()
            items = makes
        }

        select
    }

    class CustomItemCaptionGenerator implements ItemCaptionGenerator {
        @Override
        String apply(Object item) {
            if(item instanceof Make) return (item as Make).name
            if(item instanceof Driver) return (item as Driver).name
            if(item instanceof Model) return (item as Model).name

            null
        }
    }

    private ComboBox<Model> buildModelComponent() {
        final List<Model> models = modelService.listAll()
        final ComboBox<Model> select = new ComboBox<>()

        select.with {
            emptySelectionAllowed = false
            placeholder = "Select a Model"
            itemCaptionGenerator = new CustomItemCaptionGenerator()
            items = models
        }

        select
    }

    private ComboBox<Driver> buildDriverComponent() {
        final List<Driver> drivers = driverService.listAll()
        final ComboBox<Driver> select = new ComboBox<>()

        select.with {
            emptySelectionAllowed = false
            placeholder = "Select a Driver"
            itemCaptionGenerator = new CustomItemCaptionGenerator()
            items = drivers
        }

        select
    }

    private Grid buildVehicleComponent() {
        final List<Vehicle> vehicles = vehicleService.listAll(false)
        final Grid grid = new Grid<>()

        grid.with {
            setSizeFull()
            items = vehicles
            addColumn(new VehicleValueProvider('id')).setCaption("ID")
            addColumn(new VehicleValueProvider('name')).setCaption("Name")
            addColumn(new VehicleValueProvider('make.name')).setCaption("Make")
            addColumn(new VehicleValueProvider('model.name')).setCaption("Model")
            addColumn(new VehicleValueProvider('driver.name')).setCaption("Name")
        }

        grid
    }

    class VehicleValueProvider implements ValueProvider {
        String propertyName

        VehicleValueProvider(String propertyName) {
            this.propertyName = propertyName
        }

        @Override
        Object apply(Object o) {
            switch (propertyName) {
                case 'id':
                    if ( o instanceof Vehicle) return (o as Vehicle).id
                    break
                case 'name':
                    if ( o instanceof Vehicle) return (o as Vehicle).name
                    break
                case 'model.name':
                    if ( o instanceof Vehicle) return (o as Vehicle).model.name
                    break
                case 'make.name':
                    if ( o instanceof Vehicle) return (o as Vehicle).make.name
                    break
                case 'driver.name':
                    if ( o instanceof Vehicle) return (o as Vehicle).driver.name
                    break
            }
            null
        }
    }

    static private Button buildSubmitButton() {
        final Button submitButton = new Button("Add to Garage")
        submitButton.styleName = "friendly"

        submitButton
    }

    private updateVehicle(final String eventType, final eventValue) {
        switch (eventType) {
            case 'NAME':
                if ( eventValue instanceof String ) {
                    this.vehicle.name = eventValue as String
                }
                break
            case 'MAKE':
                if ( eventValue instanceof Optional<Make> ) {
                    this.vehicle.make = (eventValue as Optional<Make>).get()
                }
                break
            case 'MODEL':
                if ( eventValue instanceof Optional<Model> ) {
                    this.vehicle.model = (eventValue as Optional<Model>).get()
                }
                break
            case 'DRIVER':
                if ( eventValue instanceof Optional<Driver> ) {
                    this.vehicle.driver = (eventValue as Optional<Driver>).get()
                }
                break
            default:
                log.error 'updateVehicle invoked with wrong eventType: {}', eventType
        }
    }

    private submit() {
        vehicleService.save(this.vehicle)
        // tag::navigateTo[]
        getUI().navigator.navigateTo(VIEW_NAME)
        // end::navigateTo[]
    }
}
