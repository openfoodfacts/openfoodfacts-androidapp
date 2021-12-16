# -----------------------------------------------------------------------------
#
# Versionomy convenience interface
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


# == Versionomy
#
# The Versionomy module contains some convenience methods for creating and
# parsing version numbers.

module Versionomy

  @default_format = nil


  class << self


    # Gets the current default format. Usually this is the "standard"
    # format returned by Versionomy::Format.standard.

    def default_format
      @default_format ||= Format.standard
    end


    # Sets the default format used by other methods of this convenience
    # interface. Usually, this is set to the "standard" format returned by
    # Versionomy::Format.standard and should not be changed.
    #
    # The format can be specified as a format object or the name of a format
    # registered with Versionomy::Format. If the format is set to nil, the
    # default_format will be reset to the "standard" format.
    #
    # Raises Versionomy::Errors::UnknownFormatError if a name is given that
    # is not registered.

    def default_format=(format_)
      if format_.kind_of?(::String) || format_.kind_of?(::Symbol)
        format_ = Format.get(format_, true)
      end
      @default_format = format_
    end


    # Create a new version number given a hash or array of values, and an
    # optional format.
    #
    # The values should either be a hash of field names and values, or an
    # array of values that will be interpreted in field order.
    #
    # The format can be specified as a format object or the name of a format
    # registered with Versionomy::Format. If the format is omitted or set
    # to nil, the default_format will be used.
    #
    # You can also optionally provide default parameters to be used when
    # unparsing this value or any derived from it.
    #
    # Raises Versionomy::Errors::UnknownFormatError if a name is given that
    # is not registered.

    def create(values_=nil, format_=nil, unparse_params_=nil)
      if format_.kind_of?(::Hash) && unparse_params_.nil?
        unparse_params_ = format_
        format_ = nil
      end
      if format_.kind_of?(::String) || format_.kind_of?(::Symbol)
        format_ = Format.get(format_, true)
      end
      format_ ||= default_format
      Value.new(values_ || [], format_, unparse_params_)
    end


    # Create a new version number given a string to parse, and an optional
    # format.
    #
    # The format can be specified as a format object or the name of a format
    # registered with Versionomy::Format. If the format is omitted or set
    # to nil, the default_format will be used.
    #
    # The parameter hash, if present, will be passed as parsing parameters
    # to the format.
    #
    # Raises Versionomy::Errors::UnknownFormatError if a name is given that
    # is not registered.
    #
    # May raise Versionomy::Errors::ParseError if parsing failed.

    def parse(str_, format_=nil, parse_params_=nil)
      if format_.kind_of?(::Hash) && parse_params_.nil?
        parse_params_ = format_
        format_ = nil
      end
      if format_.kind_of?(::String) || format_.kind_of?(::Symbol)
        format_ = Format.get(format_, true)
      end
      format_ ||= default_format
      format_.parse(str_, parse_params_)
    end


    # Convenience method for creating a version number using the Semantic
    # Versioning format (see http://semver.org/).
    #
    # You may pass a string to parse, or a hash with the following keys, all
    # of which are optional:
    # <tt>:major</tt>::
    #   Major version number
    # <tt>:minor</tt>::
    #   Minor version number
    # <tt>:patch</tt>::
    #   Patch version number
    # <tt>:prerelease_suffix</tt>::
    #   A prerelease suffix (e.g. "b2")
    #
    # May raise Versionomy::Errors::ParseError if parsing failed.

    def semver(input_)
      if input_.kind_of?(::Hash)
        create(input_, :semver)
      else
        parse(input_.to_s, :semver)
      end
    end


    # Get the version of the given module as a Versionomy::Value.
    # Tries a number of common approaches to embedding version numbers into
    # modules, such as string or array constants, submodules containing
    # constants, or module method calls.
    # Returns the version number, or nil if it wasn't found or couldn't
    # be interpreted.

    def version_of(mod_)
      version_ = nil
      [:VERSION, :VERSION_STRING, :GemVersion].each do |sym_|
        if mod_.const_defined?(sym_)
          version_ = mod_.const_get(sym_)
          break
        end
      end
      if version_.kind_of?(::Module)
        if version_.const_defined?(:STRING)
          version_ = version_.const_get(:STRING)
        elsif version_.const_defined?(:VERSION)
          version_ = version_.const_get(:VERSION)
        elsif version_.const_defined?(:MAJOR) && version_.const_defined?(:MINOR) && version_.const_defined?(:TINY)
          version_ = Value.new([version_.const_get(:MAJOR), version_.const_get(:MINOR), version_.const_get(:TINY)], :standard)
        end
      end
      unless version_.kind_of?(::String) || version_.kind_of?(::Array) || version_.kind_of?(Value)
        [:version, :release].each do |sym_|
          if mod_.respond_to?(sym_)
            version_ = mod_.send(sym_)
            break
          end
        end
      end
      if version_.kind_of?(::String)
        version_ = parse(version_, :standard) rescue nil
      elsif version_.kind_of?(::Array)
        version_ = create(version_, :standard) rescue nil
      elsif !version_.kind_of?(Value)
        version_ = nil
      end
      version_
    end


    # Get the ruby version as a Versionomy::Value, using the builtin
    # constants RUBY_VERSION and RUBY_PATCHLEVEL.

    def ruby_version
      @ruby_version ||= begin
        version_ = parse(::RUBY_VERSION, :standard)
        if version_.release_type == :final
          version_ = version_.change({:patchlevel => ::RUBY_PATCHLEVEL},
            :patchlevel_required => true, :patchlevel_delim => '-p')
        end
        version_
      end
    end


  end

end
