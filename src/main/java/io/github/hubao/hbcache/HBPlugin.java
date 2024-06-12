package io.github.hubao.hbcache;

/*
 * Desc:
 *
 * @author hubao
 * @see 2024/6/12 20:13
 */
public interface HBPlugin {

    void init();
    void startup();
    void shutdown();
}
