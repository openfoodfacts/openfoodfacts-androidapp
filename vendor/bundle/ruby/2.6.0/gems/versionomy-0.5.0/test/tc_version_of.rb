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

    class TestVersionOf < ::Minitest::Test  # :nodoc:


      # Gems to test if we can
      GEM_LIST = {
        'activerecord' => {:require => 'active_record', :module_name => 'ActiveRecord'},
        'blockenspiel' => {:module_name => 'Blockenspiel'},
        'bundler' => {:module_name => 'Bundler'},
        'erubis' => {:module_name => 'Erubis'},
      }


      # Engine that tests each gem if it's installed
      zero_ = ::Versionomy.create(:major => 0)
      GEM_LIST.each do |name_, data_|
        unless data_.kind_of?(::Hash)
          data_ = {:module_name => data_}
        end
        begin
          gem name_
          require data_[:require] || name_
        rescue ::LoadError
          next
        end
        define_method("test_gem_#{name_}") do
          mod_ = eval(data_[:module_name])
          value_ = ::Versionomy.version_of(mod_)
          refute_nil(value_)
          refute_equal(zero_, value_)
        end
      end


    end

  end
end
