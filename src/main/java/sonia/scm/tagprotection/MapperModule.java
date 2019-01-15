package sonia.scm.tagprotection;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.plugin.Extension;

@Extension
public class MapperModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TagProtectionConfigMapper.class).to(Mappers.getMapper(TagProtectionConfigMapper.class).getClass());
    }
}
