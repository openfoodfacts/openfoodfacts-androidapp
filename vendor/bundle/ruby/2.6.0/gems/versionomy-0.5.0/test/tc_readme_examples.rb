# -----------------------------------------------------------------------------
#
# Versionomy tests of the README examples
#
# This file contains tests to ensure the README is valid
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


require 'minitest/autorun'
require 'versionomy'


module Versionomy
  module Tests  # :nodoc:

    class TestReadmeExamples < ::Minitest::Test  # :nodoc:


      # Test the README file.
      # This actually reads the README file and does some eval magic
      # to run it and ensure it works the way it claims.

      def test_readme_file
        binding_ = _get_binding
        ::File.open("#{::File.dirname(__FILE__)}/../README.rdoc") do |io_|
          running_ = false
          buffer_ = ''
          buffer_start_line_ = nil
          io_.each_line do |line_|

            # Run code in the "Some examples" section.
            running_ = false if line_ =~ /^===/
            running_ = true if line_ =~ /^=== Some examples/
            next unless running_ && line_[0,1] == ' '
            # Skip the require line
            next if line_ =~ /^\s+require/

            # If there isn't an expects clause, then collect the code into
            # a buffer to run all at once, because it might be code that
            # gets spread over multiple lines.
            delim_index_ = line_.index(' # ')
            if !delim_index_ || line_[0, delim_index_].strip.length == 0
              buffer_start_line_ ||= io_.lineno
              buffer_ << line_
              next
            end

            # At this point, we have an expects clause. First run any buffer
            # accumulated up to now.
            if buffer_.length > 0
              ::Kernel.eval(buffer_, binding_, 'README.rdoc', buffer_start_line_)
              buffer_ = ''
              buffer_start_line_ = nil
            end

            # Parse the line into an expression and an expectation
            expr_ = line_[0,delim_index_]
            expect_ = line_[delim_index_+3..-1]

            if expect_ =~ /^=> (.*)$/
              # Expect a value
              expect_value_ = ::Kernel.eval($1, binding_, 'README.rdoc', io_.lineno)
              actual_value_ = ::Kernel.eval(expr_, binding_, 'README.rdoc', io_.lineno)
              assert_equal(expect_value_, actual_value_,
                           "Values did not match on line #{io_.lineno} of README.rdoc")

            elsif expect_ =~ /^raises (.*)$/
              # Expect an exception to be raised
              expect_error_ = ::Kernel.eval($1, binding_, 'README.rdoc', io_.lineno)
              assert_raises(expect_error_) do
                ::Kernel.eval(expr_, binding_, 'README.rdoc', io_.lineno)
              end

            else
              raise "Unknown expect syntax: #{expect_.inspect}"
            end

          end
        end
      end


      def _get_binding
        binding
      end


    end

  end
end
