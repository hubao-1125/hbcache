package io.github.hubao.hbcache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/6/12 20:14
 */
@Component
public class HBApplicationListener implements ApplicationListener<ApplicationEvent> {

    @Autowired
    private List<HBPlugin> plugins;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        if (event instanceof ApplicationReadyEvent are) {

            for (HBPlugin plugin : plugins) {
                plugin.init();
                plugin.startup();
            }
        } else if (event instanceof ContextClosedEvent cce) {

            for (HBPlugin plugin : plugins) {
                plugin.shutdown();
            }
        }
    }
}
