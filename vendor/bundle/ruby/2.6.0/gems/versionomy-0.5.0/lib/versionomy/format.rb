# -----------------------------------------------------------------------------
#
# Versionomy format namespace
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


require 'thread'
require 'monitor'


module Versionomy


  # === Version number format.
  #
  # A format controls the parsing and unparsing of a version number.
  # Any time a version number is parsed from a string, a format is provided
  # to parse it. Similarly, every version number value references a format
  # that is used to unparse it back into a string.
  #
  # A format is always tied to a particular schema and knows how to parse
  # only that schema's version numbers.
  #
  # Under many circumstances, you should use the standard format, which
  # can be retrieved by calling Versionomy::Format#standard. This format
  # understands most common version numbers, including prerelease
  # (e.g. alpha, beta, release candidate, etc.) forms and patchlevels.
  #
  # You may also create your own formats, either by implementing the
  # format contract (see Versionomy::Format::Base), or by using the
  # Versionomy::Format::Delimiter tool, which can be used to construct
  # parsers for many version number formats.
  #
  # === Format registry
  #
  # Formats may be registered with Versionomy and given a name using the
  # methods of this module. This allows version numbers to be serialized
  # with their format. When a version number is serialized, its format
  # name is written to the stream, along with the version number's string
  # representation. When the version number is reconstructed, its format
  # is looked up by name so versionomy can determine how to parse the
  # string.
  #
  # Format names are strings that may include letters, numerals, dashes,
  # underscores, and periods. By convention, periods are used as namespace
  # delimiters. Format names without a namespace (that is, with no periods)
  # are considered reserved for standard versionomy formats. If you define
  # your own format, you should use a name that includes a namespace (e.g.
  # "mycompany.LibraryVersion") to reduce the chance of name collisions.
  #
  # You may register formats directly using the register method, or set it
  # up to be autoloaded on demand. When a format is requested, if it has
  # not been registered explicitly, Versionomy looks for a format definition
  # file for that format. Such a file has the name of the format, with the
  # ".rb" extension for ruby (e.g. "mycompany.LibraryVersion.rb") and must
  # be located in a directory in versionomy's format lookup path. By
  # default, the directory containing versionomy's predefined formats
  # (such as "standard") is in this path. However, you may add your own
  # directories using the add_directory method. This lets you autoload your
  # own formats. A format definition file itself must contain ruby code
  # that defines the format and registers it using the correct name. See
  # the files in the "lib/versionomy/format_definitions/" directory for
  # examples.

  module Format

    @mutex = ::Mutex.new
    @load_mutex = ::Monitor.new
    @directories = [::File.expand_path(::File.dirname(__FILE__)+'/format_definitions')]
    @names_to_formats = {}
    @formats_to_names = {}

    class << self


      # Add a directory to the format path.
      #
      # The format path is an array of directory paths. These directories
      # are searched for format definitions if a format name that has not
      # been registered is requested.
      #
      # If high_priority_ is set to true, the directory is added to the
      # front of the lookup path; otherwise it is added to the back.

      def add_directory(path_, high_priority_=false)
        path_ = ::File.expand_path(path_)
        @mutex.synchronize do
          unless @directories.include?(path_)
            if high_priority_
              @directories.unshift(path_)
            else
              @directories.push(path_)
            end
          end
        end
      end


      # Get the format with the given name.
      #
      # If the given name has not been defined, attempts to autoload it from
      # a format definition file. See the description of the Format module
      # for details on this procedure.
      #
      # If the given name still cannot be resolved, and strict is set to
      # true, raises Versionomy::Errors::UnknownFormatError. If strict is
      # set to false, returns nil if the given name cannot be resolved.

      def get(name_, strict_=false)
        name_ = _check_name(name_)
        format_ = @mutex.synchronize{ @names_to_formats[name_] }
        if format_.nil?
          # Attempt to find the format in the directory path
          dirs_ = @mutex.synchronize{ @directories.dup }
          dirs_.each do |dir_|
            path_ = "#{dir_}/#{name_}.rb"
            if ::File.readable?(path_)
              @load_mutex.synchronize{ ::Kernel.load(path_) }
            end
            format_ = @mutex.synchronize{ @names_to_formats[name_] }
            break unless format_.nil?
          end
        end
        if format_.nil? && strict_
          raise Errors::UnknownFormatError, name_
        end
        format_
      end


      # Determines whether a format with the given name has been registered
      # explicitly. Does not attempt to autoload the format.

      def registered?(name_)
        name_ = _check_name(name_)
        @mutex.synchronize{ @names_to_formats.include?(name_) }
      end


      # Register the given format under the given name.
      #
      # Valid names may contain only letters, digits, underscores, dashes,
      # and periods.
      #
      # Raises Versionomy::Errors::FormatRedefinedError if the name has
      # already been defined.

      def register(name_, format_, silent_=false)
        name_ = _check_name(name_)
        @mutex.synchronize do
          if @names_to_formats.include?(name_)
            unless silent_
              raise Errors::FormatRedefinedError, name_
            end
          else
            @names_to_formats[name_] = format_
            @formats_to_names[format_.object_id] = name_
          end
        end
      end


      # Get the canonical name for the given format, as a string.
      # This is the first name the format was registered under.
      #
      # If the given format was never registered, and strict is set to true,
      # raises Versionomy::Errors::UnknownFormatError. If strict is set to
      # false, returns nil if the given format was never registered.

      def canonical_name_for(format_, strict_=false)
        name_ = @mutex.synchronize{ @formats_to_names[format_.object_id] }
        if name_.nil? && strict_
          raise Errors::UnknownFormatError
        end
        name_
      end


      private

      def _check_name(name_)  # :nodoc:
        name_ = name_.to_s
        unless name_ =~ /\A[\w.-]+\z/
          raise ::ArgumentError, "Illegal name: #{name_.inspect}"
        end
        name_
      end


    end

  end


  # Versionomy::Formats is an alias for Versionomy::Format, for backward
  # compatibility with version 0.1.0 code. It is deprecated; use
  # Versionomy::Format instead.
  Formats = Format


end
