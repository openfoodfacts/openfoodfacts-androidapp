# -----------------------------------------------------------------------------
#
# Versionomy semver format implementation
#
# -----------------------------------------------------------------------------
# Copyright 2010 Daniel Azuma
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


    # Get the semver format.
    # This is identical to calling <tt>get('semver')</tt>.
    #
    # The semver format is designed to conform to the Semantic Versioning
    # spec by Tom Preston-Warner. See http://semver.org/
    #
    # For the exact annotated definition of the semver schema and format,
    # see the source code for Versionomy::Format::Semver#create.

    def self.semver
      get('semver')
    end


    # This is a namespace for the implementation of the semver schema
    # and format.

    module Semver


      # Extra methods added to version values that use the semver schema.

      module ExtraMethods


        # Returns true if the version is a prerelease version-- that is,
        # if the prerelease_suffix is nonempty.

        def prerelease?
          prerelease_suffix.length > 0
        end


        # Returns the release for this version.
        # For example, converts "1.2.0a1" to "1.2.0".
        # Non-prerelease versions return themselves unchanged.

        def release
          prerelease? ? self.change(:prerelease_suffix => '') : self
        end


        # Returns true if this version is compatible with the given version,
        # according to the Semantic Versioning specification.
        # For example, 1.1.0 is compatible with 1.0.0 but not vice versa,
        # 1.1.1 and 1.1.0 are compatible with each other, while 1.0.0 and
        # 2.0.0 are mutually incompatible.

        def compatible_with?(version_)
          self.major == version_.major ? self.minor >= version_.minor : false
        end


      end


      # Create the semver format.
      # This method is called internally when Versionomy loads the semver
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the schema and format
      # definition DSLs.

      def self.create

        # The following is the definition of the semver schema
        schema_ = Schema.create do

          # The first field has the default value of 1. All other fields
          # have a default value of 0. Thus, the default version number
          # overall is "1.0".
          field(:major, :type => :integer, :default_value => 1) do
            field(:minor, :type => :integer) do
              field(:patch, :type => :integer) do
                field(:prerelease_suffix, :type => :string) do
                  to_compare do |a_, b_|
                    a_.length == 0 ? (b_.length == 0 ? 0 : 1) : (b_.length == 0 ? -1 : a_ <=> b_)
                  end
                end
              end
            end
          end

          # An alias
          alias_field(:special_suffix, :prerelease_suffix)

          # Add the methods in this module to each value
          add_module(Format::Semver::ExtraMethods)
        end

        # The following is the definition of the standard format. It
        # understands the standard schema defined above.
        Format::Delimiter.new(schema_) do

          # All version number strings must start with the major version.
          # Unlike other fields, it is not preceded by the usual "dot"
          # delimiter, but it can be preceded by a "v" indicator.
          field(:major) do
            recognize_number(:delimiter_regexp => 'v?', :default_delimiter => '')
          end

          # The remainder of the core version number are represented as
          # integers delimited by periods. These fields are required.
          field(:minor) do
            recognize_number
          end
          field(:patch) do
            recognize_number
          end

          # The optional prerelease field is represented as a string
          # beginning with an alphabetic character.
          field(:prerelease_suffix) do
            recognize_regexp('[a-zA-Z][0-9a-zA-Z-]*', :default_value_optional => true,
                             :delimiter_regexp => '', :default_delimiter => '')
          end
        end
      end


    end


    register('semver', Format::Semver.create, true)


  end


  module Conversion


    # This is a namespace for the implementation of the conversion between
    # the semver and standard formats.

    module Semver


      # Create the conversion from standard to semver format.
      # This method is called internally when Versionomy loads the semver
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the conversion DSLs.

      def self.create_standard_to_semver

        # We'll use a parsing conversion.
        Conversion::Parsing.new do

          # Sanity check the original value and make sure it doesn't
          # include fields that we don't support.
          to_modify_original_value do |value_, convert_params_|
            if value_.has_field?(:patchlevel) && value_.patchlevel != 0
              raise Errors::ConversionError, 'Cannot convert a version with a patchlevel to semver'
            end
            if value_.tiny2 != 0
              raise Errors::ConversionError, 'Cannot convert a version more than three fields to semver'
            end
            value_
          end

          # We're going to modify how the standard format version is
          # unparsed, so the semver format will have a better chance
          # of parsing it.
          to_modify_unparse_params do |params_, convert_params_|

            # All three fields are required
            params_[:minor_required] = true
            params_[:tiny_required] = true

            # If the standard format version has a prerelease notation,
            # make sure it isn't set off using a delimiter.
            params_[:release_type_delim] = ''
            params_[:development_version_delim] = ''
            params_[:development_minor_delim] = '-'
            params_[:alpha_version_delim] = ''
            params_[:alpha_minor_delim] = '-'
            params_[:beta_version_delim] = ''
            params_[:beta_minor_delim] = '-'
            params_[:release_candidate_version_delim] = ''
            params_[:release_candidate_minor_delim] = '-'
            params_[:preview_version_delim] = ''
            params_[:preview_minor_delim] = '-'

            # If the standard format version includes a "v" prefix, strip it
            params_[:major_delim] = nil

            params_
          end

        end

      end


      # Create the conversion from semver to standard format.
      # This method is called internally when Versionomy loads the semver
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the conversion DSLs.

      def self.create_semver_to_standard

        # We'll use a parsing conversion.
        Conversion::Parsing.new do

          # Handle the case where the semver version ends with a string
          # field, e.g. "1.0b". We want to treat this like "1.0b0" rather
          # than "1.0-2" since the semver semantic states that this is a
          # prerelease version. So we add 0 to the end of the parsed string
          # if it ends in a letter.
          to_modify_string do |str_, convert_params_|
            str_.gsub(/([[:alpha:]])\z/, '\10')
          end

        end

      end


    end


    register(:standard, :semver, Conversion::Semver.create_standard_to_semver, true)
    register(:semver, :standard, Conversion::Semver.create_semver_to_standard, true)


  end


end
