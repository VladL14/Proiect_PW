package org.openapitools.configuration;

import org.openapitools.model.DiceFace;
import org.openapitools.model.MatchStatus;
import org.openapitools.model.RoundStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * This class provides Spring Converter beans for the enum models in the OpenAPI specification.
 *
 * By default, Spring only converts primitive types to enums using Enum::valueOf, which can prevent
 * correct conversion if the OpenAPI specification is using an `enumPropertyNaming` other than
 * `original` or the specification has an integer enum.
 */
@Configuration(value = "org.openapitools.configuration.enumConverterConfiguration")
public class EnumConverterConfiguration {

    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.diceFaceConverter")
    Converter<String, DiceFace> diceFaceConverter() {
        return new Converter<String, DiceFace>() {
            @Override
            public DiceFace convert(String source) {
                return DiceFace.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.matchStatusConverter")
    Converter<String, MatchStatus> matchStatusConverter() {
        return new Converter<String, MatchStatus>() {
            @Override
            public MatchStatus convert(String source) {
                return MatchStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.roundStatusConverter")
    Converter<String, RoundStatus> roundStatusConverter() {
        return new Converter<String, RoundStatus>() {
            @Override
            public RoundStatus convert(String source) {
                return RoundStatus.fromValue(source);
            }
        };
    }

}
