package nl.finan.finq.configuration;

import nl.eernie.jmoribus.configuration.Configuration;
import nl.eernie.jmoribus.configuration.DefaultConfiguration;
import nl.finan.finq.embeder.ConfigurationFactory;
import nl.finan.finq.embeder.WebStoryReporter;
import nl.finan.finq.factory.BeanFactory;
import nl.finan.finq.steps.Step;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Arrays;

@Stateless
public class ConfigurationFactoryImpl implements ConfigurationFactory {

    @EJB
    WebStoryReporter reporter;

    @Override
    public Configuration getConfiguration() {
        Configuration configuration = new DefaultConfiguration();
        configuration.addSteps(Arrays.<Object>asList(new Step()));
        return configuration;
    }

    @Override
    public Configuration getConfigurationWithReporter(Long reportId) {
        Configuration configuration = new DefaultConfiguration();
        configuration.addSteps(Arrays.<Object>asList(new Step()));
        configuration.addReporter(reporter);
        return configuration;
    }
}
