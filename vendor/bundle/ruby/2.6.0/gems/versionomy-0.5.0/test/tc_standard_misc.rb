# -----------------------------------------------------------------------------
#
# Versionomy basic tests on standard schema
#
# This file contains tests for the basic use cases on the standard schema
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
require 'yaml'

module Versionomy
  module Tests  # :nodoc:

    class TestStandardMisc < ::Minitest::Test  # :nodoc:


      # Test "prerelase?" custom method

      def test_method_prereleasep
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :beta, :beta_version => 3)
        assert_equal(true, value_.prerelease?)
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :final, :patchlevel => 1)
        assert_equal(false, value_.prerelease?)
        value_ = ::Versionomy.create(:major => 2, :tiny => 1)
        assert_equal(false, value_.prerelease?)
      end


      # Test "relase" custom method

      def test_method_release
        value_ = ::Versionomy.create(:major => 1, :minor => 9, :tiny => 2, :release_type => :alpha, :alpha_version => 4)
        value2_ = value_.release
        assert_equal([1, 9, 2, 0, :final, 0, 0], value2_.values_array)
        value_ = ::Versionomy.create(:major => 1, :minor => 9, :tiny => 2)
        value2_ = value_.release
        assert_equal(value_, value2_)
      end


      # Test marshalling

      def test_marshal
        value_ = ::Versionomy.create(:major => 1, :minor => 9, :tiny => 2, :release_type => :alpha, :alpha_version => 4)
        str_ = ::Marshal.dump(value_)
        value2_ = ::Marshal.load(str_)
        assert_equal(value_, value2_)
      end


      # Test YAML

      def test_yaml
        value_ = ::Versionomy.create(:major => 1, :minor => 9, :tiny => 2, :release_type => :alpha, :alpha_version => 4)
        str_ = ::YAML.dump(value_)
        value2_ = ::YAML.load(str_)
        assert_equal(value_, value2_)
      end


    end

  end
end
