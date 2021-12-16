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


    # Get the standard format.
    # This is identical to calling <tt>get('standard')</tt>.
    #
    # The standard format is designed to handle most commonly-used version
    # number forms, and allow parsing and comparison between them.
    #
    # The standard schema is the heart of this format, providing a
    # common structure for most version numbers.
    #
    # It begins with four numeric fields:
    # "<tt>major.minor.tiny.tiny2</tt>".
    #
    # The next field, <tt>:release_type</tt>, defines the remaining
    # structure. The release type can be one of these symbolic values:
    # <tt>:development</tt>, <tt>:alpha</tt>, <tt>:beta</tt>,
    # <tt>:preview</tt>, <tt>:release_candidate</tt>, <tt>:release</tt>.
    #
    # Depending on that value, additional fields become available. For
    # example, the <tt>:alpha</tt> value enables the fields
    # <tt>:alpha_version</tt> and <tt>:alpha_minor</tt>, which represent
    # version number fields after the "a" alpha specifier. i.e. "2.1a30"
    # has an alpha_version of 30. "2.1a30.2" also has an alpha_minor of 2.
    # Similarly, the <tt>:beta</tt> release_type value enables the fields
    # <tt>:beta_version</tt> and <tt>:beta_minor</tt>. A release_type
    # of <tt>:release</tt> enables <tt>:patchlevel</tt> and
    # <tt>:patchlevel_minor</tt>, to support versions like "1.8.7p72".
    #
    # The format itself is a delimiter-based format that understands a
    # wide variety of string representations of this version schema.
    # Examples of supported syntax include:
    #
    #  2.0
    #  2.0.42.10
    #  2.0b2
    #  2.0rc15
    #  2.0-5
    #  2.0p5
    #  2.0 Alpha 1
    #  2.0a5.3
    #  2.1.42.10-4.3
    #
    # Because the standard format is based on Versionomy::Format::Delimiter,
    # a number of parameters are available for parsing and unparsing. See
    # the documentation for the delimiter class for more information.
    #
    # Two of the fields have styles that can be set when unparsing.
    # The <tt>:release_type</tt> field can be unparsed as either
    # <tt>:long</tt> style (e.g. "1.0alpha2") or <tt>:short</tt> style
    # (e.g. "1.0a2"). The patchlevel field can be unparsed as either
    # <tt>:number</tt> style (e.g. "2.1-1") or <tt>:letter</tt> style
    # (e.g. "2.1a"). Most fields can have their delimiter specified during
    # unparsing as well.
    #
    # For the exact annotated definition of the standard schema and format,
    # see the source code for Versionomy::Format::Standard#create.

    def self.standard
      get('standard')
    end


    # This is a namespace for the implementation of the Standard schema
    # and format.

    module Standard


      # Extra methods added to version values that use the standard schema.

      module ExtraMethods


        # Returns true if the version is a prerelease version

        def prerelease?
          self.release_type != :final
        end


        # Returns the release for this version.
        # For example, converts "1.2.0a1" to "1.2.0".
        # Non-prerelease versions return themselves.

        def release
          self.change(:release_type => :final)
        end


      end


      # Create the standard format.
      # This method is called internally when Versionomy loads the standard
      # format, and you should not need to call it again. It is documented
      # so that you can inspect its source code from RDoc, since the source
      # contains useful examples of how to use the schema and format
      # definition DSLs.

      def self.create

        # The following is the definition of the standard schema
        schema_ = Schema.create do

          # The major field has the default value of 1. Most other fields
          # have a default value of 0. Thus, the default version number
          # overall is "1.0".
          # We first create the core version fields "major.minor.tiny.tiny2".
          field(:major, :default_value => 1) do
            field(:minor) do
              field(:tiny) do
                field(:tiny2) do

                  # The next field is a symbolic field that specifies the
                  # release type: e.g. beta, release candidate, release, etc.
                  field(:release_type, :type => :symbol) do

                    # Development releases are typically expressed like
                    # "1.0d3" and are intended for unstable development
                    # progress. Bumping the release type will change it to
                    # alpha.
                    symbol(:development, :bump => :alpha)

                    # Alpha releases are typically expressed like "1.0a2" and
                    # are intended for internal testing.
                    # Bumping the release type advances to beta.
                    symbol(:alpha, :bump => :beta)

                    # Beta releases are typically expressed like "1.0b2" and
                    # are intended for external or public testing.
                    # Bumping the release type advances to release candidate.
                    symbol(:beta, :bump => :release_candidate)

                    # Release candidate releases are typically expressed like
                    # "1.0rc2" and are intended for final public testing
                    # prior to release.
                    # Bumping the release type advances to final release.
                    symbol(:release_candidate, :bump => :final)

                    # Preview releases represent an alternative release type
                    # progression, and are typically used for public testing
                    # similar to beta or release candidate.
                    # Bumping the release type advances to final release.
                    symbol(:preview, :bump => :final)

                    # This type represents a final release. This is the
                    # default value for the release_type field if no value is
                    # explicitly provided.
                    # Bumping the release type has no effect.
                    symbol(:final, :bump => :final)
                    default_value(:final)

                    # If the release type is development, these fields are
                    # made available to indicate which development release
                    # is being represented.
                    field(:development_version, :only => :development,
                          :default_value => 1) do
                      field(:development_minor)
                    end

                    # If the release type is alpha, these fields are made
                    # available to indicate which alpha release is being
                    # represented.
                    field(:alpha_version, :only => :alpha, :default_value => 1) do
                      field(:alpha_minor)
                    end

                    # If the release type is beta, these fields are made
                    # available to indicate which beta release is being
                    # represented.
                    field(:beta_version, :only => :beta, :default_value => 1) do
                      field(:beta_minor)
                    end

                    # If the release type is release candidate, these fields
                    # are made available to indicate which release candidate
                    # is being represented.
                    field(:release_candidate_version, :only => :release_candidate,
                          :default_value => 1) do
                      field(:release_candidate_minor)
                    end

                    # If the release type is preview, these fields are made
                    # available to indicate which preview release is being
                    # represented.
                    field(:preview_version, :only => :preview, :default_value => 1) do
                      field(:preview_minor)
                    end

                    # If the release type is final, these fields are made
                    # available to indicate an optional patchlevel.
                    field(:patchlevel, :only => :final) do
                      field(:patchlevel_minor)
                    end
                  end
                end
              end
            end
          end

          # Add the methods in this module to each value
          add_module(Format::Standard::ExtraMethods)
        end

        # The following is the definition of the standard format. It
        # understands the standard schema defined above.
        Format::Delimiter.new(schema_) do

          # All version number strings must start with the major version.
          # Unlike other fields, it is not preceded by the usual "dot"
          # delimiter, but it can be preceded by a "v" indicator.
          field(:major) do
            recognize_number(:delimiter_regexp => '(v\s?)?', :default_delimiter => '')
          end

          # The remainder of the core version number are represented as
          # integers delimited by periods by default. Each is also dependent
          # on the presence of the previous field, so :requires_previous_field
          # retains its default value of true. Finally, they can be optional
          # in an unparsed string if they are set to the default value of 0.
          field(:minor) do
            recognize_number(:default_value_optional => true)
          end
          field(:tiny) do
            recognize_number(:default_value_optional => true)
          end
          field(:tiny2) do
            recognize_number(:default_value_optional => true)
          end

          # The release type field is the most complex field because of the
          # variety of syntaxes we support. The basic strategy is to map
          # a few specific sets of characters as signaling particular release
          # types. For example, the "a" in "1.0a5" signals an alpha release.
          # If no such release type marker is found, it defaults to the final
          # release type.
          # We set up two styles, a short style and a long style. Short style
          # syntax looks like "1.0a5". Long syntax looks more like
          # "1.0 Alpha 5". The parsed value retains knowledge of which style
          # it came from so it can be reconstructed when the value is unparsed.
          # Note that we turn requires_previous_field off because the release
          # type syntax markers don't require any particular set of the core
          # version number fields to be present. "1.0a5" and "1.0.0.0a5" are
          # both valid version numbers.
          field(:release_type, :requires_previous_field => false,
                :default_style => :short) do
            # Some markers require a prerelease version (e.g. the 5 in
            # "1.0a5") while others don't (e.g. "1.9.2dev"). This is because
            # the syntax "1.0a" looks like a patchlevel syntax. So some of
            # the following recognizers set requires_next_field while others
            # do not.
            # Also note that we omit the value <tt>:final</tt>. This is
            # because that value is signaled by the absence of any syntax in
            # the version string, including the absence of any delimiters.
            # So we just allow it to fall through to the default.

            recognize_regexp_map(:style => :long, :default_delimiter => '',
                                 :delimiter_regexp => '-|_|\.|\s?') do
              map(:development, 'dev')
              map(:alpha, 'alpha')
              map(:beta, 'beta')
              map(:preview, 'preview')
            end
            recognize_regexp_map(:style => :short, :default_delimiter => '',
                                 :delimiter_regexp => '-|_|\.|\s?') do
              map(:release_candidate, 'rc')
              map(:preview, 'pre')
            end
            recognize_regexp_map(:style => :long, :default_delimiter => '',
                                 :delimiter_regexp => '-|_|\.|\s?') do
              map(:release_candidate, 'rc')
            end
            recognize_regexp_map(:style => :short, :default_delimiter => '',
                                 :delimiter_regexp => '-|_|\.|\s?',
                                 :requires_next_field => true) do
              map(:development, 'd')
              map(:alpha, 'a')
              map(:beta, 'b')
            end
          end

          # The main prerelease version may sometimes be optional, so we
          # mark it as optional here. If it is required, that will be
          # signalled by requires_next_field on the release_type field.
          # Minor prerelease versions are always optional.
          # Note that we override the default_value (normally 1) and set
          # it to 0 if a main prerelease version is not present. This is
          # so schema-oriented operations like bumping will set the value
          # to 1, while parsing a string will yield 0 when the field is
          # missing (e.g. we want "1.9.2dev" to mean "1.9.2dev0".)
          field(:development_version, :default_value => 0) do
            recognize_number(:delimiter_regexp => '-|_|\.|\s?', :default_delimiter => '',
                             :default_value_optional => true)
          end
          field(:development_minor) do
            recognize_number(:default_value_optional => true)
          end
          field(:alpha_version, :default_value => 0) do
            recognize_number(:delimiter_regexp => '-|_|\.|\s?', :default_delimiter => '',
                             :default_value_optional => true)
          end
          field(:alpha_minor) do
            recognize_number(:default_value_optional => true)
          end
          field(:beta_version, :default_value => 0) do
            recognize_number(:delimiter_regexp => '-|_|\.|\s?', :default_delimiter => '',
                             :default_value_optional => true)
          end
          field(:beta_minor) do
            recognize_number(:default_value_optional => true)
          end
          field(:release_candidate_version, :default_value => 0) do
            recognize_number(:delimiter_regexp => '-|_|\.|\s?', :default_delimiter => '',
                             :default_value_optional => true)
          end
          field(:release_candidate_minor) do
            recognize_number(:default_value_optional => true)
          end
          field(:preview_version, :default_value => 0) do
            recognize_number(:delimiter_regexp => '-|_|\.|\s?', :default_delimiter => '',
                             :default_value_optional => true)
          end
          field(:preview_minor) do
            recognize_number(:default_value_optional => true)
          end

          # The patchlevel field does not require the previous field (which is
          # release_type). Here we also set up two styles: a numeric style and
          # a letter style. So "1.0a" and "1.0-1" are equivalent.
          field(:patchlevel, :requires_previous_field => false,
                :default_value_optional => true, :default_style => :number) do
            recognize_number(:style => :number, :default_delimiter => '-',
                             :delimiter_regexp => '(-|_|\.|\s?)(p|u)|-|_')
            recognize_letter(:style => :letter, :default_delimiter => '',
                             :delimiter_regexp => '-|_|\.|\s?',
                             :expected_follower_regexp => '\z')
          end
          field(:patchlevel_minor) do
            recognize_number(:default_value_optional => true)
          end

          # By default, we require that at least the major and minor fields
          # appear in an unparsed version string.
          default_unparse_params(:required_fields => [:minor, :development_version, :alpha_version,
            :beta_version, :release_candidate_version, :preview_version])
        end
      end


    end


    register('standard', Format::Standard.create, true)


  end

end
