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


module Versionomy
  module Tests  # :nodoc:

    class TestStandardBasic < ::Minitest::Test  # :nodoc:


      # Test the default version value.

      def test_default_value
        value_ = ::Versionomy.create
        assert_equal(1, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
      end


      # Test an arbitrary release value.

      def test_release_value_1
        value_ = ::Versionomy.create(:major => 1, :tiny => 4, :tiny2 => 2, :patchlevel => 5)
        assert_equal(1, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(4, value_.tiny)
        assert_equal(2, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(5, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
        assert_equal(false, value_.has_field?(:prerelase_version))
        assert_equal(false, value_.has_field?(:prerelase_minor))
      end


      # Test an arbitrary release value.

      def test_release_value_2
        value_ = ::Versionomy.create(:major => 0, :minor => 3)
        assert_equal(0, value_.major)
        assert_equal(3, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:final, value_.release_type)
        assert_equal(0, value_.patchlevel)
        assert_equal(0, value_.patchlevel_minor)
        assert_equal(false, value_.has_field?(:prerelase_version))
        assert_equal(false, value_.has_field?(:prerelase_minor))
      end


      # Test an arbitrary preview value.

      def test_preview_value_1
        value_ = ::Versionomy.create(:major => 2, :minor => 3, :release_type => :preview, :preview_version => 3)
        assert_equal(2, value_.major)
        assert_equal(3, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:preview, value_.release_type)
        assert_equal(3, value_.preview_version)
        assert_equal(0, value_.preview_minor)
        assert_equal(false, value_.has_field?(:patchlevel))
        assert_equal(false, value_.has_field?(:patchlevel_minor))
      end


      # Test an arbitrary preview value.

      def test_preview_value_2
        value_ = ::Versionomy.create(:major => 2, :minor => 3, :release_type => :preview)
        assert_equal(2, value_.major)
        assert_equal(3, value_.minor)
        assert_equal(0, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:preview, value_.release_type)
        assert_equal(1, value_.preview_version)
        assert_equal(0, value_.preview_minor)
        assert_equal(false, value_.has_field?(:patchlevel))
        assert_equal(false, value_.has_field?(:patchlevel_minor))
      end


      # Test an arbitrary beta value.

      def test_beta_value
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :beta, :beta_version => 3)
        assert_equal(2, value_.major)
        assert_equal(0, value_.minor)
        assert_equal(1, value_.tiny)
        assert_equal(0, value_.tiny2)
        assert_equal(:beta, value_.release_type)
        assert_equal(3, value_.beta_version)
        assert_equal(0, value_.beta_minor)
        assert_equal(false, value_.has_field?(:prerelase_version))
        assert_equal(false, value_.has_field?(:prerelase_minor))
        assert_equal(false, value_.has_field?(:patchlevel))
        assert_equal(false, value_.has_field?(:patchlevel_minor))
      end


      # Test specifying fields by index.

      def test_field_get_index
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :beta, :beta_version => 3)
        assert_equal(2, value_[0])
        assert_equal(0, value_[1])
        assert_equal(1, value_[2])
        assert_equal(0, value_[3])
        assert_equal(:beta, value_[4])
        assert_equal(3, value_[5])
        assert_equal(0, value_[6])
      end


      # Test specifying fields by name.

      def test_field_get_name
        value_ = ::Versionomy.create(:major => 2, :tiny => 1, :release_type => :beta, :beta_version => 3)
        assert_equal(2, value_[:major])
        assert_equal(0, value_[:minor])
        assert_equal(1, value_[:tiny])
        assert_equal(0, value_[:tiny2])
        assert_equal(:beta, value_[:release_type])
        assert_equal(3, value_[:beta_version])
        assert_equal(0, value_[:beta_minor])
      end


    end

  end
end
