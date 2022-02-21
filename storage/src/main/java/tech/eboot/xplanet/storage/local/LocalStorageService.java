package tech.eboot.xplanet.storage.local;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import tech.eboot.xplanet.storage.StorageService;

/**
 * @author TangThree
 * Created on 2022/2/21 11:32
 **/

@Service
public class LocalStorageService implements StorageService, InitializingBean
{
    @Override
    public void afterPropertiesSet() throws Exception
    {
        load();
    }

    private void load()
    {

    }
}
