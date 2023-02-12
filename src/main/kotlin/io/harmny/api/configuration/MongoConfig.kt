package io.harmny.api.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import javax.annotation.PostConstruct


@Configuration
class MongoConfig(
    private val mappingMongoConverter: MappingMongoConverter,
) {

    @PostConstruct
    private fun setDefaultMongoTypeMapper() {
        mappingMongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))
    }
}
