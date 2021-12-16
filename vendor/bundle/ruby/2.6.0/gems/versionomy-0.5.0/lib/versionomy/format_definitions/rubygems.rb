# -----------------------------------------------------------------------------
#
# Versionomy standard format implementation
#
# -----------------------------------------------------------------------------
# Copyright 2008 Daniel Azuma
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the copyright holder, nor the names of any other
#   contributors to this software, may be used to endorse or promote products
#   derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# -----------------------------------------------------------------------------
;


module Versionomy

  module Format


    # Get the rubygems format.
    # This is identical to calling <tt>get('rubygems')</tt>.
    #
    # The rubygems format is designed to be parse-compatible with the
    # Gem::Version class used in rubygems. The only caveat is, whereas
    # Gem::Version handles an arbitrary number of fields, this format is
    # limited to a maximum of 8.
    #
    # For the exact annotated definition of the rubygems schema and format,
    # see the source code for Versionomy::Format::Rubygems#create.

    def self.rubygems
      get('rubygems')
    end


    # This is a namespace for the implementation of the Rubygems schema
    # and format.

    module Rubygems


      # Extra methods added to version values that use the rubygems schema.

      module ExtraMethods


        # Returns true if the version is a prerelease version-- that is,
        # if any of the fields is non-numeric.
        #
        # This behaves the same as the Gem::Version#prerelease? method
        # in rubygems.

        def prerelease?
          values_array.any?{ |val_| val_.kind_of?(::String) }
        end


        # Returns the release for this version.
        # For example, converts "1.2.0.a.1" to "1.2.0".
        # Non-prerelease versions return themselves.
        #
        # This behaves the same as the Gem::Version#release method
        # in rubygems.

        def release
          values_ = []
          self.each_field_object do |field_, val_|
            break unless val_.kind_of?(::Integer)
            values_ << val_
          end
          Value.new(values_, self.format, self.unparse_params)
        end


        # Returns a list of the field values, in field order, with
        # trailing zeroes stripped off.
        #
        # This behaves the same as the Gem::Version#parts method
        # in rubygems.

        def parts
          unless defined?(@parts)
            @parts = values_array
            @parts.pop while @parts.size > 1 && @parts.last == 0
          end
          @parts
        end


      end


      # Create the rubygems format.
      # This method is called internally when Versionomy loads the rubygems
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the schema and format
      # definition DSLs.

      def self.create

        # The following is the definition of the rubygems schema
        schema_ = Schema.create do

          # Global comparison function
          to_compare_type(:string) do |a_, b_|
            if a_.kind_of?(::Integer)
              if b_.kind_of?(::Integer)
                a_ <=> b_
              else
                1
              end
            else
              if b_.kind_of?(::Integer)
                -1
              else
                a_ <=> b_
              end
            end
          end

          # Global canonicalization function
          to_canonicalize_type(:string) do |val_|
            if val_.kind_of?(::Integer)
              val_
            else
              val_ = val_.to_s
              if val_ =~ /\A\d*\z/
                val_.to_i
              else
                val_
              end
            end
          end

          # The first field has the default value of 1. All other fields
          # have a default value of 0. Thus, the default version number
          # overall is "1.0".
          field(:field0, :type => :integer, :default_value => 1) do
            field(:field1, :type => :string) do
              field(:field2, :type => :string) do
                field(:field3, :type => :string) do
                  field(:field4, :type => :string) do
                    field(:field5, :type => :string) do
                      field(:field6, :type => :string) do
                        field(:field7, :type => :string)
                      end
                    end
                  end
                end
              end
            end
          end

          # Some field aliases providing alternate names for major fields
          alias_field(:major, :field0)
          alias_field(:minor, :field1)

          # Add the methods in this module to each value
          add_module(Format::Rubygems::ExtraMethods)
        end

        # The following is the definition of the rubygems format. It
        # understands the rubygems schema defined above.
        Format::Delimiter.new(schema_) do

          # All version number strings must start with the major version.
          # Unlike other fields, it is not preceded by any delimiter.
          field(:field0) do
            recognize_number(:delimiter_regexp => '', :default_delimiter => '')
          end

          # The remainder of the version number are represented as strings
          # or integers delimited by periods by default. Each is also
          # dependent on the presence of the previous field, so
          # :requires_previous_field retains its default value of true.
          # Finally, they can be optional in an unparsed string if they are
          # set to the default value of 0.
          field(:field1) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field2) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field3) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field4) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field5) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field6) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end
          field(:field7) do
            recognize_regexp('[0-9a-zA-Z]+', :default_value_optional => true)
          end

          # By default, we require that at least the first two fields
          # appear in an unparsed version string.
          default_unparse_params(:required_fields => [:field1])
        end
      end


    end


    register('rubygems', Format::Rubygems.create, true)


  end


  module Conversion


    # This is a namespace for the implementation of the conversion between
    # the rubygems and standard formats.

    module Rubygems


      # Create the conversion from standard to rubygems format.
      # This method is called internally when Versionomy loads the rubygems
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the conversion DSLs.

      def self.create_standard_to_rubygems

        # We'll use a parsing conversion.
        Conversion::Parsing.new do

          # We're going to modify how the standard format version is
          # unparsed, so the rubygems format will have a better chance
          # of parsing it.
          to_modify_unparse_params do |params_, convert_params_|

            params_ ||= {}

            # If the standard format version has a prerelease notation,
            # make sure it is set off using a delimiter that the rubygems
            # format can recognize. So instead of "1.0b2", we force the
            # unparsing to generate "1.0.b.2".
            params_[:release_type_delim] = '.'
            params_[:development_version_delim] = '.'
            params_[:alpha_version_delim] = '.'
            params_[:beta_version_delim] = '.'
            params_[:release_candidate_version_delim] = '.'
            params_[:preview_version_delim] = '.'

            # If the standard format version has a patchlevel notation,
            # force it to use the default number rather than letter style.
            # So instead of "1.2c", we force the unparsing to generate
            # "1.2-3".
            params_[:patchlevel_style] = nil

            # If the standard format version has a patchlevel notation,
            # force it to use the default delimiter of "-" so the rubygems
            # format will recognize it. So instead of "1.9.1p243", we force
            # the unparsing to generate "1.9.1-243".
            params_[:patchlevel_delim] = nil

            # If the standard format version includes a "v" prefix, strip
            # it because rubygems doesn't like it.
            params_[:major_delim] = nil

            params_
          end

          # Standard formats sometimes allow hyphens and spaces in field
          # delimiters, but the rubygems format requires periods. So modify
          # the unparsed string to conform to rubygems's expectations.
          to_modify_string do |str_, convert_params_|
            str_.gsub(/[\.\s-]+/, '.')
          end

        end

      end


      # Create the conversion from rubygems to standard format.
      # This method is called internally when Versionomy loads the rubygems
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the conversion DSLs.

      def self.create_rubygems_to_standard

        # We'll use a parsing conversion.
        Conversion::Parsing.new do

          # Handle the case where the rubygems version ends with a string
          # field, e.g. "1.0.b". We want to treat this like "1.0b0" rather
          # than "1.0-2" since the rubygems semantic states that this is a
          # prerelease version. So we add 0 to the end of the parsed string
          # if it ends in a letter.
          to_modify_string do |str_, convert_params_|
            str_.gsub(/([[:alpha:]])\z/, '\10')
          end

        end

      end


    end


    register(:standard, :rubygems, Conversion::Rubygems.create_standard_to_rubygems, true)
    register(:rubygems, :standard, Conversion::Rubygems.create_rubygems_to_standard, true)


  end


end
